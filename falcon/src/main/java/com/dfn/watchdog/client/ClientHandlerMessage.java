package com.dfn.watchdog.client;

import com.dfn.watchdog.client.util.ClientConstants;
import com.dfn.watchdog.client.util.DataSupplier;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.client.AsyncResponse;
import com.dfn.watchdog.commons.messages.client.ClientRouteResponse;
import com.dfn.watchdog.commons.messages.client.RouteResponseAggregated;
import com.dfn.watchdog.commons.messages.cluster.ChangeLinks;
import com.dfn.watchdog.commons.messages.cluster.ChangeView;
import com.dfn.watchdog.commons.messages.monitoring.ExternalLinkStatus;
import com.dfn.watchdog.commons.messages.monitoring.GatewayMetrics;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import com.dfn.watchdog.commons.messages.monitoring.OmsMetrics;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * Handler for the watchdog-server side connection.
 */
public class ClientHandlerMessage extends SimpleChannelInboundHandler<EventMessage> {
    private static final Logger logger = LogManager.getLogger(ClientHandlerMessage.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, EventMessage message) throws Exception {
        if (message instanceof NodeMetrics) {
            NodeMetrics metrics = (NodeMetrics) message;
            WatchdogClient.INSTANCE.getView().getNode(metrics.getNodeId(), metrics.getNodeType()).setMetrics(metrics);
            WatchdogClient.INSTANCE.getDataHolder().addToCpuUsage(metrics);
//            writeToWebsocket(metrics.toJson());
            writeToWebsocket(ClientConstants.nodeMetricToJson(metrics));
            writeToWebsocket(WatchdogClient.INSTANCE.getSystemMetrics()
                    .calculate(WatchdogClient.INSTANCE.getView()).toJson());
            logger.debug(metrics);
        } else if (message instanceof ChangeView) {
            View view = ((ChangeView) message).getView();
            WatchdogClient.INSTANCE.installView(view);
            logger.info(view);
//            writeToWebsocket(WatchdogClient.INSTANCE.getView().toJsonMetric());
            writeToWebsocket(ClientConstants.toJsonMetric(WatchdogClient.INSTANCE.getView()));
            writeToWebsocket(DataSupplier.getViewForTree(WatchdogClient.INSTANCE.getView()));
            writeToWebsocket(DataSupplier.getViewForBlocks(
                    WatchdogClient.INSTANCE.getView(), WatchdogClient.INSTANCE.getLinks()));
        } else if (message instanceof ChangeLinks) {
            WatchdogClient.INSTANCE.addLinks(((ChangeLinks) message).getLinks());
            writeToWebsocket(DataSupplier.getViewForBlocks(
                    WatchdogClient.INSTANCE.getView(), WatchdogClient.INSTANCE.getLinks()));
        } else if (message instanceof GatewayMetrics) {
            GatewayMetrics gatewayMetrics = (GatewayMetrics) message;
            WatchdogClient.INSTANCE.getSystemMetrics()
                    .setClients(getCumulativeClients(gatewayMetrics)
                    .getConnectedClients());
            WatchdogClient.INSTANCE.getSystemMetrics().setTps(gatewayMetrics.getTransactionCount());
            writeToWebsocket(gatewayMetrics.toJson());
        } else if (message instanceof OmsMetrics) {
            OmsMetrics omsMetrics = (OmsMetrics) message;
            WatchdogClient.INSTANCE.getSystemMetrics().setRequests(omsMetrics.getRequestCount());
            writeToWebsocket(omsMetrics.toJson());
        } else if (message instanceof AsyncResponse) {
            processAsyncResponse((AsyncResponse) message);
        } else if (message instanceof ExternalLinkStatus) {
            WatchdogClient.INSTANCE.addExternalLink((ExternalLinkStatus) message);
            writeToWebsocket(DataSupplier.getViewForBlocks(
                    WatchdogClient.INSTANCE.getView(), WatchdogClient.INSTANCE.getLinks()));
        } else {
            logger.info("Received unhandled message: " + message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel Active callback");
        writeToWebsocket("{\"messageType\" : \"server_connect\", \"connected\" :true}");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel Inactive callback");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Channel caught an exception", cause);
        ctx.close();
    }

    private void processAsyncResponse(AsyncResponse response) {
        if (response instanceof ClientRouteResponse) {
            ClientRouteResponse clientRouteResponse = (ClientRouteResponse) response;
            Phaser phaser = WatchdogClient.INSTANCE.getRequestMap().get(clientRouteResponse.getRequestId()).getPhaser();
            RouteResponseAggregated aggregatedResponse = (RouteResponseAggregated)
                    WatchdogClient.INSTANCE.getRequestMap().get(clientRouteResponse.getRequestId()).getResponse();

            /*aggregatedResponse.addResponse(clientRouteResponse);
            phaser.arriveAndDeregister();*/


            if (response.isInitial()) {
                logger.info("Response for client route, waiting for "
                        + clientRouteResponse.getNodes() + " more responses");
                aggregatedResponse.setEndPoints(clientRouteResponse.getNodes());
                if (clientRouteResponse.getNodes() > 0) {
                    phaser.bulkRegister(clientRouteResponse.getNodes() - 1);
                } else {
                    phaser.arriveAndDeregister();
                }
                aggregatedResponse.addResponse(clientRouteResponse);
            } else {
                logger.info("Response for client route, "
                        + clientRouteResponse.getSource() + " --> " + clientRouteResponse.getRoute());
                aggregatedResponse.addResponse(clientRouteResponse);
                phaser.arriveAndDeregister();
            }
        }
    }

    private void writeToWebsocket(String message) {
        WatchdogClient.INSTANCE.broadcastToWeb(message);
    }

    /**
     * Get the aggregate number of clients to be displayed in Falcon.
     * @param metrics
     * @return metrics
     */
    private GatewayMetrics getCumulativeClients(GatewayMetrics metrics) {
        int clientCount = 0;
        WatchdogClient.INSTANCE.getConnectedClients().put(metrics.getNodeType().toString() + metrics.getNodeId(), metrics.getConnectedClients());
        for (Map.Entry<String, Integer> map: WatchdogClient.INSTANCE.getConnectedClients().entrySet()) {
            clientCount += map.getValue();
        }
        metrics.setConnectedClients(clientCount);
        return metrics;
    }
}