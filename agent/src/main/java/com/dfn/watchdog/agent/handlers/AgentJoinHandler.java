package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.agent.util.AgentWriteListener;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.exceptions.InvalidConfigurationError;
import com.dfn.watchdog.commons.messages.cluster.*;
import com.dfn.watchdog.commons.messages.secondary.SwapAgent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Mange the joining process to the cluster.
 * <p>
 * Sends a Join message to the server when channel connects.
 */
public class AgentJoinHandler extends SimpleChannelInboundHandler<JoinMessage> {
    private static final Logger logger = LoggerFactory.getLogger(AgentJoinHandler.class);
    private ScheduledFuture heartbeatFuture;

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentJoinHandler() {
        super(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Sending Join Message");
        Join join = new Join(WatchdogAgent.INSTANCE.getNode());
        ctx.writeAndFlush(join).addListener(new AgentWriteListener(join, ctx.channel()));
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        stopHeartbeats();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, JoinMessage message) throws Exception {
        if (message instanceof JoinAck) {
            logger.info("Received Join Ack from server");
            final State currentState = WatchdogAgent.INSTANCE.getNode().getState();
            WatchdogAgent.INSTANCE.installView(((JoinAck) message).getView());
            WatchdogAgent.INSTANCE.getNode().changeState(State.INITIALIZING);
            WatchdogAgent.INSTANCE.getNode().setChannel(ctx.channel());
            ctx.writeAndFlush(new ChangeState(WatchdogAgent.INSTANCE.getNode()));
            scheduleHeartbeats(ctx);

            ctx.channel().eventLoop().parent()
                    .submit(() -> WatchdogAgent.INSTANCE.getListener().initializing(currentState))
                    .addListener(future -> {
                        if ((Boolean) future.get()) {
                            Node node = WatchdogAgent.INSTANCE.getNode();
                            synchronized (node) {
                                node.changeState(State.CONNECTING);
                                ChangeState changeState = new ChangeState(node);
                                ctx.writeAndFlush(changeState)
                                        .addListener(new AgentWriteListener(changeState, ctx.channel()));
                                logger.info("Agent successful initialized : " + node);
                            }
                        } else {
                            Node node = WatchdogAgent.INSTANCE.getNode().changeState(State.FAILED);
                            ChangeState changeState = new ChangeState(node);
                            ctx.writeAndFlush(changeState)
                                    .addListener(new AgentWriteListener(changeState, ctx.channel()));
                        }
                    });
        } else if (message instanceof JoinNack) {
            logger.info("Received Join Nack from server");
            logger.warn(message.toString());
            stopHeartbeats();
            throw new InvalidConfigurationError(message.toString());
        } else if (message instanceof SwapAgent) {
            logger.info("Received SwapAgent from server");
            if (((SwapAgent) message).isDoSwapServer()) {
                logger.info("going to swap server");
                WatchdogAgent.INSTANCE.swapAgentServer(((SwapAgent) message).getServerIp(), ((SwapAgent) message).getPort());
            }
            stopHeartbeats();
        }
    }

    private void scheduleHeartbeats(ChannelHandlerContext ctx) {
        EventLoopGroup group = ctx.channel().eventLoop().parent();
        heartbeatFuture = group.scheduleAtFixedRate(() -> {
                    ctx.channel().writeAndFlush(new Heartbeat(WatchdogAgent.INSTANCE.getNode()));
                }, WatchdogAgent.INSTANCE.getProperties().heartbeatDelay(),
                WatchdogAgent.INSTANCE.getProperties().heartbeatPeriod(), TimeUnit.SECONDS);
    }

    private void stopHeartbeats() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
        }
    }
}
