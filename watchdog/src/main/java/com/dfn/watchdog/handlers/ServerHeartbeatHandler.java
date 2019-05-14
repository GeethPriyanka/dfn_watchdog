package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.messages.cluster.Heartbeat;
import com.dfn.watchdog.commons.messages.cluster.HeartbeatAck;
import com.dfn.watchdog.commons.messages.cluster.HeartbeatMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Heartbeat message tx/rx.
 * <p>
 * Read agent heartbeat message and sends acknowledgement.
 */
public class ServerHeartbeatHandler extends SimpleChannelInboundHandler<HeartbeatMessage> {
    private static final Logger logger = LogManager.getLogger(ServerHeartbeatHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ServerHeartbeatHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartbeatMessage message) throws Exception {
        Heartbeat heartbeat = (Heartbeat) message;
        Node node = WatchdogServer.INSTANCE.getView().getNode(heartbeat.getId(), heartbeat.getType());
        node.updateLastHeartbeat();
        ctx.writeAndFlush(new HeartbeatAck(WatchdogServer.INSTANCE.getView().getBroadcastView()));
        logger.debug("Heartbeat Message from: " + node.getName() + " channel: "+ctx.channel().remoteAddress());
    }
}