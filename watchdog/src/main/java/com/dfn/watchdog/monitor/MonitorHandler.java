package com.dfn.watchdog.monitor;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.client.ClientRouteRequest;
import com.dfn.watchdog.commons.messages.client.ClientRouteResponse;
import com.dfn.watchdog.commons.messages.cluster.ChangeLinks;
import com.dfn.watchdog.commons.messages.cluster.ChangeView;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * All requests from client is handled here.
 */
public class MonitorHandler extends SimpleChannelInboundHandler<EventMessage> {
    private static final Logger logger = LogManager.getLogger(MonitorHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EventMessage message) throws Exception {
        if (message instanceof ClientRouteRequest) {
            ClientRouteRequest clientRequest = (ClientRouteRequest) message;
            long clientId = clientRequest.getClientId();
            int requestId = clientRequest.getRequestId();

            String nextString = WatchdogServer.INSTANCE.getClientFromDb(clientId);
            if (nextString == null) {
                nextString = "No route in DB";
            } else {
                nextString = "OMS-" + nextString;
            }

            int noOfNodes = 0;
            for (Node node : WatchdogServer.INSTANCE.getView().getAllNodes(NodeType.GATEWAY).values()) {
                if (node.getState() == State.CONNECTED) {
                    noOfNodes++;
                    node.getChannel().writeAndFlush(clientRequest);
                }
            }

            ctx.writeAndFlush(new ClientRouteResponse(requestId, true)
                    .setNodes(noOfNodes).setClientId(clientId).setRoute(nextString).setSource("Server"));
            logger.info("Route request received, sending result from db");
        } else {
            logger.info("Unknown message received from client: " + message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Monitor channel Active" + ctx.channel().remoteAddress());
        ctx.writeAndFlush(new ChangeView(WatchdogServer.INSTANCE.getView()));
        ctx.writeAndFlush(new ChangeLinks(WatchdogServer.INSTANCE.getView().getLinks()));
        WatchdogServer.INSTANCE.getView().setMonitorChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Monitor channel Inactive");
        WatchdogServer.INSTANCE.getView().setMonitorChannel(null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Monitor channel caught an exception", cause);
        ctx.close();
        WatchdogServer.INSTANCE.getView().setMonitorChannel(null);
    }
}

