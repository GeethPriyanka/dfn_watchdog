package com.dfn.watchdog.handlers.monitoring;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.messages.cluster.ChangeLinks;
import com.dfn.watchdog.commons.messages.monitoring.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Heartbeat message tx/rx.
 *
 * Read agent heartbeat message and sends acknowledgement.
 */
public class ServerMonitoringHandler extends SimpleChannelInboundHandler<MonitoringMessage> {
    private static final Logger logger = LogManager.getLogger(ServerMonitoringHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ServerMonitoringHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MonitoringMessage message) throws Exception {
        if (message instanceof JvmMetrics) {
            JvmMetrics metrics = (JvmMetrics) message;
            NodeMetrics nodeMetrics = WatchdogServer.INSTANCE.getView()
                    .getNode(metrics.getNodeId(), metrics.getNodeType()).getMetrics();
            nodeMetrics.setJvmMetrics(metrics);
            WatchdogServer.INSTANCE.routeToMonitor(nodeMetrics);
        } else if (message instanceof LinkStatus) {
            Map<String, Map<String, State>> links = WatchdogServer.INSTANCE.getView().addToLinks((LinkStatus) message);
            WatchdogServer.INSTANCE.routeToMonitor(new ChangeLinks(links));
            logger.info("Re-routing linkStatus message: " + message);
        } else if (message instanceof ExternalLinkStatus) {
            WatchdogServer.INSTANCE.routeToMonitor(message);
            logger.info("Re-routing externallinkStatus message: " + message);
        } else if (message instanceof GatewayMetrics) {
            setCumulativeClients((GatewayMetrics) message);
            WatchdogServer.INSTANCE.routeToMonitor(message);
        } else if (message instanceof OmsMetrics) {
            WatchdogServer.INSTANCE.routeToMonitor(message);
        } else {
            logger.warn("Unhandled message received");
        }
    }

    /**
     * Set the connected clients in the map at Watchdog end.
     * Can use this in Watchdog server if needed to access the connected clients of each node.
     * NOTE: Key for the map (String) = NodeType + NodeID
     * @param metrics
     */
    private void setCumulativeClients(GatewayMetrics metrics) {
        int clientCount = 0;
        WatchdogServer.INSTANCE.getConnectedClients().put(metrics.getNodeType().toString() + metrics.getNodeId(), metrics.getConnectedClients());
        for (Map.Entry<String, Integer> map: WatchdogServer.INSTANCE.getConnectedClients().entrySet()) {
            clientCount += map.getValue();
        }
    }
}