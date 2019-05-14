package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.commons.messages.cluster.HeartbeatAck;
import com.dfn.watchdog.commons.messages.cluster.HeartbeatMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heartbeat message tx/rx.
 * <p>
 * Schedule heartbeat in every heartbeatPeriod.
 * Compare view version in heartbeat ack.
 */
public class AgentHeartbeatHandler extends SimpleChannelInboundHandler<HeartbeatMessage> {
    private static final Logger logger = LoggerFactory.getLogger(AgentHeartbeatHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentHeartbeatHandler() {
        super(false);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HeartbeatMessage message) throws Exception {
        HeartbeatAck heartbeatAck = (HeartbeatAck) message;
        logger.debug("Heartbeat Ack Message to: " + WatchdogAgent.INSTANCE.getNode().getName() + " from: " + ctx.channel().localAddress());
        if (WatchdogAgent.INSTANCE.getView().getVersion() != heartbeatAck.getView().getVersion()) {
            logger.warn("Changing view from version: " + WatchdogAgent.INSTANCE.getView().getVersion() +
                    ", to version: " + heartbeatAck.getView().getVersion());
            WatchdogAgent.INSTANCE.installView(heartbeatAck.getView());
        }
    }
}
