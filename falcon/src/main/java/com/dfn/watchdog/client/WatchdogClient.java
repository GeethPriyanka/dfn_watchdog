package com.dfn.watchdog.client;

import com.dfn.watchdog.client.database.DatabaseUtil;
import com.dfn.watchdog.client.util.ClientProperties;
import com.dfn.watchdog.client.util.DataHolder;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.exceptions.InvalidConfigurationError;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.client.AsyncRequest;
import com.dfn.watchdog.commons.messages.client.AsyncResponse;
import com.dfn.watchdog.commons.messages.monitoring.ExternalLinkStatus;
import com.dfn.watchdog.commons.messages.monitoring.SystemMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton implementation of the Client.
 * Reads the configuration from watchdog-client.yml and fire up the client.
 * Connects to the server and Cassandra.
 * <p>
 * Can be connected via a web client.
 */

public enum WatchdogClient {
    INSTANCE;

    private final Logger logger = LogManager.getLogger(WatchdogClient.class);
    private final int threads = 5;
    private EventLoopGroup eventLoop;
    private View view;
    private ClientProperties properties;
    private Channel channel;
    private Channel wsChannel;
    private List<Channel> webSessions;
    private Bootstrap bootstrap;
    private Map<String, Map<String, State>> links;

    private DataHolder dataHolder;
    private SystemMetrics systemMetrics;

    private Map<String, Map<String, ExternalLinkStatus>> externalLinks;

    private Map<Integer, AsyncRequest> requestMap;

    private volatile Map<String, Integer> connectedClients = new HashMap<>();

    private SlaMapConfiguration slaMapConfig;

    private ServiceConfiguration services;

    WatchdogClient() {
        view = new View();
    }

    /**
     * Main method, will configure and fire up the application
     */
    public void run() throws InvalidConfigurationError {
        if (properties == null) {
            throw new InvalidConfigurationError("Configure before running!");
        } else {
            DatabaseUtil.getInstance(properties).initialize();  // needs properties to initialize database server.
            try {
                fetchSlaAsMap("slamap.yaml");
                fetchServices();    // Fetch data from services.yaml
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        connect().listen().poll();
        new ClientApplication().run();
    }

    public WatchdogClient configure(String configFile) throws InvalidConfigurationError {
        logger.info("Loading client configuration....");
        InputStream configStream = null;
        try {
            if (configFile == null) {
                configStream = WatchdogClient.class.getClassLoader().getResourceAsStream("watchdog-client.yml");
            } else {
                configStream = new FileInputStream(configFile);
            }
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            properties = mapper.readValue(configStream, ClientProperties.class);
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

        webSessions = new ArrayList<>();
        requestMap = new HashMap<>();
        links = new HashMap<>();
        systemMetrics = new SystemMetrics();
        externalLinks = new HashMap<>();
        return this;
    }

    private WatchdogClient connect() {
        logger.info("Connecting to the server at " + properties.serverIp() + ":" + properties.port());
        bootstrap = new Bootstrap();
        eventLoop = new NioEventLoopGroup(threads);
        try {
            bootstrap.group(eventLoop)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(properties.serverIp(), properties.port())
                    .handler(new ClientInitializer());
        } catch (Exception e) {
            logger.error("Error connecting to the watchdog server", e);
        }
        channel = bootstrap.connect().channel();

        return this;
    }

    private WatchdogClient listen() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.TRACE))
                    .childHandler(new WebsocketInitializer());

            wsChannel = serverBootstrap.bind(properties.host(), properties.port() + 1).sync().channel();
        } catch (Exception e) {
            logger.error("error: ", e);
        }

        logger.info("Listening at " + (properties.port() + 1));
        return this;
    }

    private WatchdogClient poll() {
        if (properties.elasticSearch()) {
//            eventLoop.scheduleAtFixedRate(new DataPoller(), 1, 2, TimeUnit.SECONDS);//poll on each minute
            logger.warn("ElasticSearch is removed. this thread need to be removed if not used for other cases");
        } else {
            logger.warn("ElasticSearch is disabled. TPS metric will be absent");
        }
        return this;
    }

    public ChannelFuture reconnectClient() {
        ChannelFuture future = bootstrap.connect();
        channel = future.channel();
        return future;
    }

    public void installView(View recentView) {
        Map<NodeType, Map<Short, Node>> oldNodeMap = view.getNodeMap();
        Map<NodeType, Map<Short, Node>> newNodeMap = recentView.getNodeMap();

        for (Map.Entry<NodeType, Map<Short, Node>> entry : oldNodeMap.entrySet()) {
            for (Map.Entry<Short, Node> nodeEntry : entry.getValue().entrySet()) {
                Node recentViewNode = newNodeMap.get(entry.getKey()).get(nodeEntry.getKey());
                recentViewNode.setMetrics(nodeEntry.getValue().getMetrics());

                recentViewNode.getName();
                nodeEntry.getValue().getName();
            }
        }
        recentView.copyLinks(view);
        view = recentView;

        if (dataHolder == null) {
            dataHolder = new DataHolder(recentView);
        }

        view.rePopulateLinks();
    }

    /**
     * Fetch services from services.yaml file.
     * @throws IOException
     */
    private void fetchServices() throws IOException {
        InputStream configStream = new FileInputStream("falcon/services.yaml");
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        this.services = mapper.readValue(configStream, ServiceConfiguration.class);
    }

    private void fetchSlaAsMap(String file) throws IOException {
        InputStream configStream = WatchdogClient.class.getClassLoader().getResourceAsStream(file);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        this.slaMapConfig = mapper.readValue(configStream, SlaMapConfiguration.class);
    }

    public View getView() {
        return view;
    }

    public ClientProperties getProperties() {
        return properties;
    }

    public Map<String, Map<String, ExternalLinkStatus>> getExternalLinks() {
        return externalLinks;
    }

    public void addExternalLink(ExternalLinkStatus linkStatus) {
        String source = linkStatus.getSourceNode();
        String target = linkStatus.getExternalNodeName();
        if (externalLinks.containsKey(source)) {
            externalLinks.get(source).put(target, linkStatus);
        } else {
            Map<String, ExternalLinkStatus> innerMap = new HashMap<>();
            innerMap.put(target, linkStatus);
            externalLinks.put(source, innerMap);
        }

        Node node = view.getNode(source);
        Map<String, Node> nodeMap = view.getAllNodes(node.getType());
        for (String nodeName : nodeMap.keySet()) {
            Map<String, ExternalLinkStatus> innerMap;
            if (externalLinks.containsKey(nodeName)) {
                innerMap = externalLinks.get(nodeName);
                if (!innerMap.containsKey(target)) {
                    innerMap.put(target, new ExternalLinkStatus(nodeName, linkStatus.getDestinationNode(), State.CLOSED, target));
                }
            } else {
                innerMap = new HashMap<>();
                innerMap.put(target, new ExternalLinkStatus(nodeName, linkStatus.getDestinationNode(), State.CLOSED, target));
                externalLinks.put(nodeName, innerMap);
            }
        }
    }

    public Map<Integer, AsyncRequest> getRequestMap() {
        return requestMap;
    }

    public AsyncResponse getAsyncResult(AsyncRequest request) {
        logger.info("Number of pending requests: " + requestMap.size());
        requestMap.put(request.getRequestId(), request);
        request.getPhaser().register();
        channel.writeAndFlush(request);

        request.getPhaser().arriveAndAwaitAdvance();
        requestMap.remove(request.getRequestId());
        return request.getResponse();
    }

    public Map<String, Map<String, State>> getLinks() {
        return links;
    }

    public void addLinks(Map<String, Map<String, State>> links) {
        this.links = links;
    }

    public DataHolder getDataHolder() {
        return dataHolder;
    }

    public SystemMetrics getSystemMetrics() {
        return systemMetrics;
    }

    public void addWebSession(Channel webChannel) {
        webSessions.add(webChannel);
    }

    public void removeWebSession(Channel webChannel) {
        webSessions.remove(webChannel);
    }

    public void broadcastToWeb(String message) {
        if (!webSessions.isEmpty()) {
            Channel c;
            for (int i = 0; i < webSessions.size(); i++) { // foreach can cause concurrent modifition as this list changes dynamically
                c = webSessions.get(i);
                if (c != null) {
                    c.writeAndFlush(new TextWebSocketFrame(message));
                }
            }
        } else {
            logger.info("No active web sessions.");
        }
    }

    public void sendToServer(EventMessage message) {
        channel.writeAndFlush(message);
    }

    public Map<String, Integer> getConnectedClients() {
        return connectedClients;
    }

    public ServiceConfiguration getServices() {
        return services;
    }

    public SlaMapConfiguration getSlaMapConfig() {
        return slaMapConfig;
    }
}
