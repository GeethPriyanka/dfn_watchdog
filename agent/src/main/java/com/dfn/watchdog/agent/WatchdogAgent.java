package com.dfn.watchdog.agent;

import com.dfn.watchdog.agent.listeners.AgentCallbackListener;
import com.dfn.watchdog.agent.listeners.AgentCallbackListenerEmpty;
import com.dfn.watchdog.agent.listeners.AgentCallbackListenerSimple;
import com.dfn.watchdog.agent.util.AgentProperties;
import com.dfn.watchdog.agent.util.ServerConfigFactory;
import com.dfn.watchdog.commons.*;
import com.dfn.watchdog.commons.exceptions.InvalidConfigurationError;
import com.dfn.watchdog.commons.messages.inquery.RouteRequest;
import com.dfn.watchdog.commons.messages.secondary.WDSecondary;
import com.dfn.watchdog.commons.stat.Statistics;
import com.dfn.watchdog.commons.stat.StatisticsCommon;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton implementation of the Agent library.
 * Reads the configuration from watchdog-agent.yml and fire up the agent.
 * Connects to the server and Cassandra.
 * <p>
 * First use configure method and then run.
 */
public enum WatchdogAgent {
    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(WatchdogAgent.class);
    private Node node;
    private View view;
    private AgentProperties properties;
    private AgentCallbackListener listener;
    private EventLoopGroup eventLoop;
    private Bootstrap bootstrap;
    private Bootstrap secondaryBootstrap;
    private AgentInitializer agentInitializer;
    private Map<Long, Short> clientRouteMap;
    private Map<Long, Short> clientRouteMapAura;
    private Map<Long, RouteRequest> routeRequestMap;

    private Statistics statistics;
    private boolean started = false;

    /**
     * Configure the server from watchdog.yml.
     *
     * @return the instance of this class.
     * @throws InvalidConfigurationError if yml configuration is wrong
     */
    public WatchdogAgent configure() throws InvalidConfigurationError {
        configure(new AgentCallbackListenerSimple(), Executors.newCachedThreadPool());
        return this;
    }

    /**
     * Configure the server from watchdog.yml.
     *
     * @param listener        to interact with the library
     * @param executorService executorService which threads are taken from
     * @return the instance of this class.
     * @throws InvalidConfigurationError if yml configuration is wrong
     */
    public WatchdogAgent configure(AgentCallbackListener listener, ExecutorService executorService)
            throws InvalidConfigurationError {
        configure(listener, executorService, null);
        return this;
    }

    /**
     * Configure the server from watchdog.yml.
     *
     * @param listener        to interact with the library
     * @param executorService executorService which threads are taken from
     * @param configFile      path to the configuration file
     * @return the instance of this class.
     * @throws InvalidConfigurationError if yml configuration is wrong
     */
    public WatchdogAgent configure(AgentCallbackListener listener, ExecutorService executorService, String configFile)
            throws InvalidConfigurationError {
        InputStream configStream = null;
        try {
            if (configFile == null) {
                configStream = WatchdogAgent.class.getClassLoader().getResourceAsStream("watchdog-agent.yml");
            } else {
                configStream = new FileInputStream(configFile);
            }
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            logger.info("Loading agent configuration...");
            properties = mapper.readValue(configStream, AgentProperties.class);
        } catch (Exception e) {
            logger.error("Cannot parse configuration. Please check configuration!", e);
            throw new InvalidConfigurationError("Cannot parse configuration. Please check configuration!");
        } finally {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException e) {
                    logger.error("Error occurred while closing ConfigFileStream", e);
                }
            }
        }

        setExecutorService(executorService);
        setListener(listener);
        return this;
    }

    public WatchdogAgent setListener(AgentCallbackListener listener) {
        this.listener = listener;
        return this;
    }

    public WatchdogAgent setExecutorService(ExecutorService executorService) {
        eventLoop = new NioEventLoopGroup(properties.agentThreads(), executorService);
        return this;
    }

    public WatchdogAgent setNode(short nodeId, String nodeType) {
        properties.setAgentId(nodeId);
        properties.setAgentType(nodeType);
        return this;
    }

    public WatchdogAgent enableWatchdog(boolean enable) {
        properties.activate(enable);
        return this;
    }

    public WatchdogAgent setServer(String serverIp, int serverPort) {
        properties.setServerIp(serverIp);
        properties.setPort(serverPort);
        return this;
    }

    public WatchdogAgent setTradeConnectivity(String serverIp, int serverPort) {
        properties.setTradeConnectIp(serverIp);
        properties.setTradeConnectPort(serverPort);
        return this;
    }

    public WatchdogAgent build() {
        if (properties.active()) {
            view = new View();
            node = new Node(properties.agentId(), properties.agentType(), State.CLOSED);
            node.setTradeIpAddress(properties.getTradeConnectIp());
            node.setTradePort(properties.getTradeConnectPort());
            clientRouteMap = new HashMap<>();
            clientRouteMapAura = new HashMap<>();
            routeRequestMap = new HashMap<>();
            statistics = new StatisticsCommon();
            String propertiesAsJson = properties.toJson();
            logger.info(propertiesAsJson);
        } else {
            listener = new AgentCallbackListenerEmpty();
            logger.warn("Watchdog is disabled! You are on your own.");
        }
        return this;
    }

    public ChannelFuture run() throws InvalidConfigurationError {
        ChannelFuture channelFuture = null;
        if (properties != null && properties.active()) {
            logger.info("Starting up Watchdog Agent...");
            if (listener == null || eventLoop == null)
                throw new InvalidConfigurationError("Please configure server before calling the run method");

            channelFuture = connectAgent();

            if (isSecondaryServerAvailable()) {
                connectSecondaryServer(ServerConfigFactory.INSTANCE.getSecondaryIp(), ServerConfigFactory.INSTANCE.getSecondaryPort());
            }
        } else if(properties == null) {
            throw new InvalidConfigurationError("Please configure server before calling the run method");
        }
        started = true;
        return channelFuture;
    }

    private ChannelFuture connectAgent() {
        bootstrap = new Bootstrap();
        agentInitializer = new AgentInitializer(properties.secured());
        try {
            bootstrap.group(eventLoop)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(properties.serverIp(), properties.port())
                    .handler(agentInitializer);
        } catch (Exception e) {
            logger.error("Cannot connect to the server: ", e);
        }
        return bootstrap.connect();
    }

    public ChannelFuture reconnectAgent() {
        return bootstrap.connect();
    }

    public void installView(View view) {
        WatchdogAgent.INSTANCE.getListener().updateConfiguration(view);
        this.view = view;
    }

    public void installView(BroadcastView broadcastView) {
        for (Map.Entry<NodeType, Map<Short, Node>> entry : broadcastView.getNodeMap().entrySet()) {
            view.getNodeMap().put(entry.getKey(), entry.getValue());
        }
        view.setVersion(broadcastView.getVersion());
        node.changeState(view.getNode(node.getId(), node.getType()).getState());
    }

    public void purgeClientRoutes() {
        logger.info("Removing all client route mappings from the agent");
        clientRouteMap.clear();
    }

    public void purgeClientRoutes(short nodeId) {
        if (nodeId == 0) {
            purgeClientRoutes();
        } else {
            logger.info("Removing all client route mappings for OMS-{}", nodeId);
            clientRouteMap.values().removeAll(Collections.singleton(nodeId));
        }
    }

    public void purgeClientRoutesAura(short nodeId) {
        if (nodeId == 0) {
            logger.info("Removing all client route mappings to Aura from the agent");
            clientRouteMapAura.clear();
        } else {
            logger.info("Removing all client route mappings for AURA-{}", nodeId);
            clientRouteMapAura.values().removeAll(Collections.singleton(nodeId));
        }
    }

    public AgentCallbackListener getListener() {
        return listener;
    }

    public Node getNode() {
        return node;
    }

    public View getView() {
        return view;
    }

    public Map<Long, Short> getClientRouteMap() {
        return clientRouteMap;
    }

    public Map<Long, RouteRequest> getRouteRequestMap() {
        return routeRequestMap;
    }

    public AgentProperties getProperties() {
        return properties;
    }

    public EventLoopGroup getEventLoop() {
        return eventLoop;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public boolean isStarted() {
        return started;
    }

    private ChannelFuture connectSecondaryServer(String ip, int port) {
        secondaryBootstrap = new Bootstrap();

        EventLoopGroup eventLoop = new NioEventLoopGroup(properties.agentThreads(), Executors.newCachedThreadPool());
        try {
            secondaryBootstrap.group(eventLoop)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(ip, port)
                    .handler(new AgentSecondaryInitializer(properties.secured()));
        } catch (Exception e) {
            logger.error("Cannot connect to the secondary server: ", e);
        }
        return secondaryBootstrap.connect();
    }

    public ChannelFuture reconnectSecondary() {
        return secondaryBootstrap.connect();
    }

    public void broadcastSecondaryConnect() {
        if (isSecondaryServerAvailable()) {
            logger.info("primary connection failure detected. Informing the secondary!");
            if (getNode().getSecondaryChannel() != null) {
                getNode().getSecondaryChannel().writeAndFlush(new WDSecondary().setNode(getNode()));
            }
        }
    }

    public void swapAgentServer(String ip, int port) {
        logger.info("primary and secondary swap called!");

        if (ip == null || port == 0 ||
                (ServerConfigFactory.INSTANCE.getPrimaryIp().equals(ip) && ServerConfigFactory.INSTANCE.getPrimaryPort() == port)) {
            logger.info("Invalid IP for port in swap message! or Already Swapped!");
            return; // already swapped
        }

        try {
            getNode().getChannel().close();
        } catch (Exception e) {
            logger.error("exception while closing the main channel", e);
        }

        try {
            getNode().getSecondaryChannel().close();
            getNode().setSecondaryChannel(null);
        } catch (Exception e) {
            logger.error("exception while closing the secondary channel", e);
        }

        ServerConfigFactory.INSTANCE.swapServers();
        logger.info("swap count: " + ServerConfigFactory.INSTANCE.getSwapCount());

        bootstrap = new Bootstrap();
        agentInitializer = new AgentInitializer(properties.secured());
        try {
            bootstrap.group(eventLoop)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(ServerConfigFactory.INSTANCE.getPrimaryIp(), ServerConfigFactory.INSTANCE.getPrimaryPort())
                    .handler(agentInitializer);
        } catch (Exception e) {
            logger.error("Cannot connect to the server: ", e);
        }

        purgeClientRoutes();
        getRouteRequestMap().clear();

        connectSecondaryServer(ServerConfigFactory.INSTANCE.getSecondaryIp(), ServerConfigFactory.INSTANCE.getSecondaryPort());
        logger.info("primary and secondary swap done!");
    }

    private boolean isSecondaryServerAvailable() {
        return ServerConfigFactory.INSTANCE.isSecondaryAvailable();
    }

    public Map<Long, Short> getClientRouteMapAura() {
        return clientRouteMapAura;
    }
}