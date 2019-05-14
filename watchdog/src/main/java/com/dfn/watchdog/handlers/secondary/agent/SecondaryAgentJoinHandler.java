package com.dfn.watchdog.handlers.secondary.agent;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.messages.secondary.SecondaryHeartBeat;
import com.dfn.watchdog.commons.messages.secondary.SecondaryJoinMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SecondaryAgentJoinHandler  extends SimpleChannelInboundHandler<SecondaryJoinMessage> {
    private static final Logger logger = LogManager.getLogger(SecondaryAgentJoinHandler.class);
    private ScheduledFuture heartbeatFuture;

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public SecondaryAgentJoinHandler() {
        super(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Sending Join Message");
        SecondaryJoinMessage join = new SecondaryJoinMessage();
        ctx.writeAndFlush(join);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        stopHeartbeats();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SecondaryJoinMessage message) throws Exception {
        if (message instanceof SecondaryJoinMessage) {
            logger.info("Received Join Ack from server. Starting heartbeat");
            scheduleHeartbeats(ctx);
            WatchdogServer.INSTANCE.setSecondaryDisconnected(false);
        }
    }

    private void scheduleHeartbeats(ChannelHandlerContext ctx) {
        EventLoopGroup group = ctx.channel().eventLoop().parent();
        heartbeatFuture = group.scheduleAtFixedRate(() -> {
                    ctx.channel().writeAndFlush(new SecondaryHeartBeat());
                }, WatchdogServer.INSTANCE.getProperties().heartbeatDelay(),
                WatchdogServer.INSTANCE.getProperties().heartbeatPeriod(), TimeUnit.SECONDS);
    }

    private void stopHeartbeats() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
        }
    }
}
