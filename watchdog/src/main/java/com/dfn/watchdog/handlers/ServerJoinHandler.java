package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.cluster.*;
import com.dfn.watchdog.commons.messages.secondary.SwapAgent;
import com.dfn.watchdog.util.ServerWriteListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Handles Agent joining process.
 * <p>
 * Sends the join ack with current view.
 * Sends join nack if Agent with same signature exists.
 */
public class ServerJoinHandler extends SimpleChannelInboundHandler<JoinMessage> {
    private static final Logger logger = LogManager.getLogger(ServerJoinHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ServerJoinHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JoinMessage message) throws Exception {
        logger.info("Agent Join Message received: " + message);

        if (!WatchdogServer.INSTANCE.isPrimary()) { // I'm the secondary. Swap the connections.
            logger.info("I'm the secondary. Swap the connections: " + message);
            SwapAgent swapAgent = new SwapAgent(true);
            swapAgent.setServerIp(WatchdogServer.INSTANCE.getProperties().getSecondaryServerIp());
            swapAgent.setPort(WatchdogServer.INSTANCE.getProperties().getSecondaryServerPort());
            ctx.writeAndFlush(swapAgent);
            return;
        }

        Node node = ((Join) message).getNode()
                .setChannel(ctx.channel())
                .setIpAddress(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
        View view = WatchdogServer.INSTANCE.getView();

        if (view.getDefaultSize(node.getType()) < node.getId()) {
            String msg = "Node: " +node.getName()+ " is Outside defined node scope. default size: " +view.getDefaultSize(node.getType());
            JoinNack ackMessage = new JoinNack(msg);
            ctx.writeAndFlush(ackMessage).addListener(new ServerWriteListener(ackMessage, ctx.channel()));
            logger.warn(view);
            logger.warn(msg);
            return;
        }

        if (view.addNode(node)) {
            JoinAck ackMessage = new JoinAck(view);
            ctx.writeAndFlush(ackMessage).addListener(new ServerWriteListener(ackMessage, ctx.channel()));

            if (WatchdogServer.INSTANCE.getProperties().isActivePassiveEnabled()) {
                //check if primary active again
                if (node.getType() == NodeType.OMS && node.getId() == (short) 1) {
                    WatchdogServer.INSTANCE.setActiveOmsId(node.getId());
                    WatchdogServer.INSTANCE.broadcastMessage(new PurgeMappings((short) 0));
                    logger.info("active oms changed to: " + node.getId());
                } else if (node.getType() == NodeType.AURA && node.getId() == (short) 1) {
                    WatchdogServer.INSTANCE.setActiveAuraId(node.getId());
                    WatchdogServer.INSTANCE.broadcastMessage(new PurgeMappings((short) 0, NodeType.AURA.name()));
                    logger.info("active aura changed to: " + node.getId());
                }
            }
        } else {
            JoinNack ackMessage = new JoinNack("Component exists with the same name");
            ctx.writeAndFlush(ackMessage).addListener(new ServerWriteListener(ackMessage, ctx.channel()));
            logger.warn(view);
            logger.warn("Component already exists with the name: " + node.getName());
        }
    }
}