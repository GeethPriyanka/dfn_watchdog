package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.client.ClientRouteResponse;
import com.dfn.watchdog.commons.messages.inquery.BlockCustomer;
import com.dfn.watchdog.commons.messages.inquery.CustomerLogin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Final handler of the pipeline.
 */
public class ServerHandlerMessage extends SimpleChannelInboundHandler<EventMessage> {
    private static final Logger logger = LogManager.getLogger(ServerHandlerMessage.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EventMessage message) throws Exception {
        if (message instanceof ClientRouteResponse) {
            WatchdogServer.INSTANCE.routeToMonitor(message);
            logger.info("Re-routing route response");
        } else if (message instanceof CustomerLogin) {
            processActiveCustomers((CustomerLogin) message);
        } else {
            logger.warn("Unhandled message received: " + message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel active, remote address: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel inactive, remote address: " + ctx.channel().remoteAddress());
        Node node = WatchdogServer.INSTANCE.getView().getNode(ctx.channel());
        if (node == null) {
            logger.warn("Couldn't find the channel in View, possibly a duplicate component");
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel registered, remote address: " + ctx.channel().remoteAddress());
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel unregistered, remote address: " + ctx.channel().remoteAddress());
        ctx.fireChannelUnregistered();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Channel caught an exception", cause);
        ctx.close();
    }

    private void processActiveCustomers(CustomerLogin message) {
        short nodeId = message.getNodeId();
        View view = WatchdogServer.INSTANCE.getView();
        boolean customerExists = false;
        for (Map.Entry<Short, Set<Long>> e : WatchdogServer.INSTANCE.getActiveCustomers().entrySet()) {
            if (e.getKey() == nodeId)
                continue;
            if (e.getValue().contains(message.getCustomerId())) {
                if (view.getNode(e.getKey(), NodeType.OMS).getState() == State.CONNECTED) {
                    customerExists = true;
                    WatchdogServer.INSTANCE.broadcastMessage(new BlockCustomer(message.getCustomerId()));
                } else {
                    //remove customer with caution
                    WatchdogServer.INSTANCE.getActiveCustomers().get(e.getKey()).remove(message.getCustomerId());
                }
            }
        }
        if (!customerExists) {
            if (WatchdogServer.INSTANCE.getActiveCustomers().get(nodeId) != null) {
                WatchdogServer.INSTANCE.getActiveCustomers().get(nodeId).add(message.getCustomerId());
            } else {
                Set<Long> set = new HashSet<>();
                set.add(message.getCustomerId());
                WatchdogServer.INSTANCE.getActiveCustomers().put(nodeId, set);
            }
        }
    }
}
