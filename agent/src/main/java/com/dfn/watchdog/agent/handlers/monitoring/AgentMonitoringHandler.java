package com.dfn.watchdog.agent.handlers.monitoring;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import com.dfn.watchdog.commons.messages.monitoring.MonitoringMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Get performance metrics of system and send to server.
 * <p>
 * Scheduler will get metrics at a fixed time intervals.
 */
public class AgentMonitoringHandler extends SimpleChannelInboundHandler<MonitoringMessage> {
    private static final Logger logger = LoggerFactory.getLogger(AgentMonitoringHandler.class);
    private ScheduledFuture future;
    private int statCounter = 0;

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentMonitoringHandler() {
        super(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final EventLoopGroup group = ctx.channel().eventLoop().parent();
        future = group.scheduleAtFixedRate(this::collectStatistics,
                WatchdogAgent.INSTANCE.getProperties().monitorPeriod(),
                WatchdogAgent.INSTANCE.getProperties().monitorPeriod(), TimeUnit.SECONDS);

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        future.cancel(true);
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, MonitoringMessage message) throws Exception {
        logger.debug("Received a monitoring message");
        collectStatistics();
    }

    private int collectStatistics() {
        JvmMetrics metrics = WatchdogAgent.INSTANCE.getStatistics()
                .collect(WatchdogAgent.INSTANCE.getNode().getMetrics().getJvmMetrics());
        WatchdogAgent.INSTANCE.getNode().getChannel().writeAndFlush(metrics);
        WatchdogAgent.INSTANCE.getListener().sendCustomMetrics();
        return 0;
    }
}
