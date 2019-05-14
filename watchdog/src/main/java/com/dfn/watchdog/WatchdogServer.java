package com.dfn.watchdog;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.db.*;
import com.dfn.watchdog.commons.exceptions.InvalidConfigurationError;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.cluster.BackupNotify;
import com.dfn.watchdog.commons.messages.cluster.ChangeLinks;
import com.dfn.watchdog.commons.messages.cluster.ChangeView;
import com.dfn.watchdog.commons.messages.cluster.PurgeMappings;
import com.dfn.watchdog.commons.messages.commands.StartEod;
import com.dfn.watchdog.commons.messages.monitoring.GatewayMetrics;
import com.dfn.watchdog.commons.messages.secondary.SwapAgent;
import com.dfn.watchdog.monitor.MonitorInitializer;
import com.dfn.watchdog.util.ServerWriteListener;
import com.dfn.watchdog.util.WatchdogProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Singleton implementation of the monitoring controller server.
 * Reads the configuration from watchdog.yml and fire up the server.
 * Listens for Agent connections, Client connection and connects with Cassandra.
 * <p>
 * First use configure method and then run.
 */
public enum WatchdogServer {
    INSTANCE;

    private final Logger logger = LogManager.getLogger(WatchdogServer.class);
    private static final short WORKER_THREADS = 10;
    private WatchdogProperties properties;
    private EventLoopGroup workerGroup;
    private volatile View view;//it might be better to synchronise every usage of the view
    private DatabaseConnection databaseConnection;
    private volatile Future deadNodeFuture;

    private Map<Short, Set<Long>> activeCustomers;
    private ChannelFuture serverChannelFuture;

    private volatile Map<String, Integer> connectedClients = new HashMap<>();
    private volatile List<Node> secondaryNodes = new ArrayList<>();

    private Bootstrap secondaryBootstrap;
    private boolean secondaryDisconnected;
    private boolean agentNotifiedDisconnection;
    private boolean isPrimary;
    private short activeOmsId;
    private short activeAuraId;

    /**
     * Starts the server.
     * Listens for Agent connections. Listen for client connection.
     * Connects to Cassandra.
     *
     * @throws InvalidConfigurationError configure() before running
     */
    public ChannelFuture run() throws InvalidConfigurationError {
        logger.info("Starting up Watchdog server...");
        if (properties == null)
            throw new InvalidConfigurationError("Please configure server before calling the run method");

        if (isSecondaryAvailable()) {
            setPrimary(properties.Primary());
        } else {
            setPrimary(true);
        }

        try {
            databaseConnection = DatabaseUtils.connectToDatabase(properties.database());
        } catch (Exception e) {
            throw new InvalidConfigurationError("Failed to start the server. DB Connectivity Failed!");
        }

        serverChannelFuture = runServer();
        if (serverChannelFuture == null)
            throw new InvalidConfigurationError("Failed to start the server");


        deadNodeFuture = workerGroup
                .scheduleAtFixedRate(new DeadNodeScanner(), properties.readTimeout(),
                        properties.readTimeout(), TimeUnit.SECONDS)
                .addListener(future -> {
                    if (future.isCancelled()) {
                        logger.error("Critical error with the server component please restart");
                    }
                });

        runMonitoring();

        if (isSecondaryAvailable()) {
            connectSecondary();
        }
        logger.info("Server started up successfully. listening for agent connections on: " + properties.port());

        return serverChannelFuture;
    }

    /**
     * Configure the server from watchdog.yml.
     *
     * @return the instance of this class.
     * @throws InvalidConfigurationError if yml configuration is wrong
     */
    public WatchdogServer configure() throws InvalidConfigurationError {
        return configure(null);
    }

    /**
     * Configure the server from watchdog.yml.
     *
     * @param configFile path to the configuration file
     * @return the instance of this class.
     * @throws InvalidConfigurationError if yml configuration is wrong
     */
    public WatchdogServer configure(String configFile) throws InvalidConfigurationError {
        InputStream configStream = null;
        try {
            if (configFile == null) {
                configStream = WatchdogServer.class.getClassLoader().getResourceAsStream("watchdog.yml");
            } else {
                configStream = new FileInputStream(configFile);
            }
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            logger.info("Loading server configuration......");
            properties = mapper.readValue(configStream, WatchdogProperties.class);
            logger.info(properties.prettyPrint());
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
        view = new View(properties.clusterConfig());
        workerGroup = new NioEventLoopGroup(WORKER_THREADS);
        activeCustomers = new HashMap<>();

        return this;
    }

    private ChannelFuture runServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        ChannelFuture channelFuture;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(properties.secured()))
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            channelFuture = bootstrap.bind(properties.host(), properties.port()).sync();
        } catch (Exception e) {
            channelFuture = null;
            logger.error("Netty connection error. Cannot bind to Server port: ", e);
        }
        return channelFuture;
    }

    private ChannelFuture runMonitoring() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup monitorWorkerGroup = new NioEventLoopGroup();
        ChannelFuture channelFuture;

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, monitorWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MonitorInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            channelFuture = bootstrap.bind(properties.host(), properties.clientPort()).sync();
        } catch (Exception e) {
            channelFuture = null;
            logger.error("Netty connection error. Cannot bind to monitor port:", e);
        }
        return channelFuture;
    }

    public View getView() {
        return view;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public Future getDeadNodeFuture() {
        return deadNodeFuture;
    }

    public ChannelFuture getServerChannelFuture() {
        return serverChannelFuture;
    }

    /**
     * Get the client route mapping from the database.
     *
     * @param clientId client id
     * @return next node to route
     */
    public String getClientFromDb(long clientId) {
        return databaseConnection.getClientFromDb(clientId);
    }

    /**
     * Set the client route mapping in the database.
     *
     * @param clientId client id
     * @param next     next node to route
     */
    public void setClientInDb(long clientId, short next) {
        databaseConnection.setClientInDb(clientId, next);
    }

    public WatchdogProperties getProperties() {
        return properties;
    }

    public Map<Short, Set<Long>> getActiveCustomers() {
        return activeCustomers;
    }

    /**
     * Broadcast the EventMessage to all of the connected Agents.
     *
     * @param message message to broadcast
     */
    public void broadcastMessage(EventMessage message) {
        for (Channel channel : view.getChannels()) {
            channel.writeAndFlush(message).addListener(new ServerWriteListener(message, channel));
        }

        if (view.getMonitorChannel() != null) {
            view.getMonitorChannel().writeAndFlush(message);
        }
    }

    /**
     * Multicast the EventMessage to all of the connected Agents.
     *
     * @param message message to multicast
     * @param type    multicast group
     * @return number of messages sent
     */
    public short multicastMessage(EventMessage message, NodeType type) {
        short messageCount = 0;
        for (Node node : view.getNodeMap().get(type).values()) {
            if (node.getChannel() != null && node.getState() == State.CONNECTED) {
                node.getChannel().writeAndFlush(message)
                        .addListener(new ServerWriteListener(message, node.getChannel()));
                messageCount++;
            }
        }
        return messageCount;
    }

    /**
     * Route the specified message to monitoring component.
     *
     * @param message message to be routed
     */
    public void routeToMonitor(EventMessage message) {
        if (view.getMonitorChannel() != null) {
            view.getMonitorChannel().writeAndFlush(message);
        }
    }

    public void startEodProcess() {
        short nodeId = view.getPrimary(NodeType.OMS);
        broadcastMessage(new StartEod(nodeId, NodeType.OMS));
    }

    public List<Node> getSecondaryNodes() {
        return secondaryNodes;
    }

    private void connectSecondary() {
        secondaryBootstrap = new Bootstrap();
        EventLoopGroup eventLoop = new NioEventLoopGroup(5);
        SecondaryAgentInitializer secondaryAgentInitializer = new SecondaryAgentInitializer(properties.secured());
        try {
            secondaryBootstrap.group(eventLoop)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(properties.getSecondaryServerIp(), properties.getSecondaryServerPort())
                    .handler(secondaryAgentInitializer);
        } catch (Exception e) {
            logger.error("Cannot connect to the secondary server: ", e);
        }
        secondaryBootstrap.connect();
        logger.info("Connected to Secondary Server : " + properties.getSecondaryServerIp() + ":" + properties.getSecondaryServerPort());
    }

    public void reconnectSecondary() {
        secondaryBootstrap.connect();
    }

    public void setSecondaryDisconnected(boolean secondaryDisconnected) {
        logger.info("setSecondaryDisconnected : " + secondaryDisconnected);
        this.secondaryDisconnected = secondaryDisconnected;
        
        if (secondaryDisconnected) {
            broadcastSecondaryDown(); // broadcast other server is down
        }
    }

    public void setAgentNotifiedDisconnection(boolean agentNotifiedDisconnection) {
        this.agentNotifiedDisconnection = agentNotifiedDisconnection;
        broadcastSecondaryDown(); // broadcast other server is down
    }

    private void broadcastSecondaryDown() {
        logger.info("broadcastSecondaryDown called");
        if (agentNotifiedDisconnection && secondaryDisconnected) {
            logger.info("Going to broadcast swap message");
            SwapAgent swapAgent = new SwapAgent(true);
            swapAgent.setServerIp(WatchdogServer.INSTANCE.getProperties().host());
            swapAgent.setPort(WatchdogServer.INSTANCE.getProperties().port());

            for (Node node : WatchdogServer.INSTANCE.getSecondaryNodes()) {
                if (node.getSecondaryChannel() != null && node.getSecondaryChannel().isActive()) {
                    node.getSecondaryChannel().writeAndFlush(swapAgent);
                    logger.info("Swap Server message sent to "+node.getName());
                }
            }

            WatchdogServer.INSTANCE.getSecondaryNodes().clear();
            setPrimary(true);
            agentNotifiedDisconnection = false;
            secondaryDisconnected = false;
        }
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
        logger.info("Primary Server: "+isPrimary);
    }

    private boolean isSecondaryAvailable() {
        return (properties.getSecondaryServerIp() != null && properties.getSecondaryServerPort() != 0);
    }

    public short getActiveOmsId() {
        return activeOmsId;
    }

    public void setActiveOmsId(short activeOmsId) {
        this.activeOmsId = activeOmsId;
    }

    public short getActiveAuraId() {
        return activeAuraId;
    }

    public void setActiveAuraId(short activeAuraId) {
        this.activeAuraId = activeAuraId;
    }

    protected class DeadNodeScanner implements Runnable {
        @Override
        public void run() {
            try {
                boolean changeView = false;
                logger.debug("Scanning for dead nodes");
                long now = System.currentTimeMillis();
                for (Node node : view.getAllNodes().values()) {
                    if (node.getState() == State.CONNECTED ||
                            node.getState() == State.CONNECTING || node.getState() == State.INITIALIZING) {
                        long lastUpdateTime = node.getLastHeartbeat();
                        if ((now - lastUpdateTime) / 1000 > properties.readTimeout()) {
                            logger.warn("Didn't hear from the node " +
                                    node.getName() + " for " + properties.readTimeout() + " seconds");
                            node.changeState(State.SUSPENDED);
                            changeView = true;
                        }
                    } else if (node.getState() == State.SUSPENDED) {
                        long lastUpdateTime = node.getLastHeartbeat();
                        sendSuspendedInfo(node);
                        if ((now - lastUpdateTime) / 1000 > properties.nodeTimeout()) {
                            logger.warn("Didn't hear from the node " +
                                    node.getName() + " for " + properties.nodeTimeout() + " seconds");
                            node.changeState(State.CLOSED);
                            processNodeFailure(node);
                            broadcastMessage(new BackupNotify(node));
                            broadcastMessage(new PurgeMappings(node.getId(), node.getType().name()));
                            changeView = true;
                        } else if ((now - lastUpdateTime) / 1000 < properties.readTimeout()) {
                            if (node.getChannel().isActive()) {
                                logger.info("Suspended node " + node.getName() + " is back online");
                                node.changeState(State.CONNECTED);
                                changeView = true;
                            } else {
                                logger.warn("Watching the suspended node " + node.getName());
                            }
                        }
                    } else {
                        logger.debug(node.getName() + " is not running.");
                    }
                }
                if (changeView) {
                    logger.warn("Periodic scan detected a view change: " + view);
                    view.refreshChannels();
                    view.populateLinks();
                    routeToMonitor(new ChangeLinks(view.getLinks()));
                    broadcastMessage(new ChangeView(view));
                }
            } catch (Exception e) {
                logger.error("Something went wrong with the background scanner thread", e);
            }
        }

        /**
         * Set 0 as the connected clients in Watchdog Connected Clients map.
         * Send 0 as the connected clients for the node to Falcon.
         * @param node
         */
        private void sendSuspendedInfo(Node node) {
            WatchdogServer.INSTANCE.getConnectedClients().put(
                    node.getType().toString() + node.getId(), 0);
            WatchdogServer.INSTANCE.routeToMonitor(new GatewayMetrics(node.getId(), node.getType(), 0));
        }

        private void processNodeFailure(Node failedNode) {
            if (failedNode.getType().equals(NodeType.OMS)) {
                logger.info("Start cleaning cassandra clientRoute");
                long start = System.currentTimeMillis();

                Node backup = view.getBackup(failedNode);
                short backupNode = backup == null ? 0 : backup.getId();

                databaseConnection.updateNodeFailure(failedNode.getId(), backupNode);

                long end = System.currentTimeMillis();
                logger.info("End cleaning cassandra clientRoute: " + (end - start) + "ms");
            }
        }
    }

    public Map<String, Integer> getConnectedClients() {
        return connectedClients;
    }
}