package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.client.ClientRouteRequest;
import com.dfn.watchdog.commons.messages.client.ClientRouteResponse;
import com.dfn.watchdog.commons.messages.inquery.BlockCustomer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Final handler of the pipeline.
 * Handles messages with least importance.
 */
public class AgentHandlerMessage extends SimpleChannelInboundHandler<EventMessage> {
    private static final Logger logger = LoggerFactory.getLogger(AgentHandlerMessage.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, EventMessage message) throws Exception {
        if (message instanceof ClientRouteRequest) {
            long clientId = ((ClientRouteRequest) message).getClientId();
            replyToClientRoute(clientId, ((ClientRouteRequest) message).getRequestId(), ctx);
        } else if (message instanceof BlockCustomer) {
            BlockCustomer blockCustomer = (BlockCustomer) message;
            WatchdogAgent.INSTANCE.getListener().blockCustomer(blockCustomer.getCustomerId());
        } else {
            logger.warn("Unhandled message received: {}", message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel Active callback");
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

    private void replyToClientRoute(long clientId, int requestId, ChannelHandlerContext ctx) {
        WatchdogAgent.INSTANCE.getEventLoop().submit(() -> {
            short nextNode = WatchdogAgent.INSTANCE.getListener().next(clientId);
            Node node = WatchdogAgent.INSTANCE.getView().getNode(nextNode, NodeType.OMS);
            ClientRouteResponse clientRouteResponse =
                    new ClientRouteResponse(requestId, false)
                            .setClientId(clientId)
                            .setSource(WatchdogAgent.INSTANCE.getNode().getName())
                            .setRoute(node.getName());
            ctx.writeAndFlush(clientRouteResponse);
            logger.info("Route message received response: " + clientId + "->" + node.getName());
        });
    }
}