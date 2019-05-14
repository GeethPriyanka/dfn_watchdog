package com.dfn.watchdog.agent.handlers.secondary;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.agent.util.ServerConfigFactory;
import com.dfn.watchdog.commons.netty.ReconnectionHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AgentSecondaryReconnectionHandler  extends ReconnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AgentSecondaryReconnectionHandler.class);

    public AgentSecondaryReconnectionHandler() {
        super();
    }

    @Override
    public void handleDisconnection(final ChannelHandlerContext ctx) {
        logger.warn("Sleeping for: " + WatchdogAgent.INSTANCE.getProperties().reconnectDelay() + " seconds");
        final EventLoopGroup eventLoopGroup = ctx.channel().eventLoop().parent();
        eventLoopGroup.schedule(() -> {
            logger.warn("Reconnecting to: " + ServerConfigFactory.INSTANCE.getSecondaryIp() + " : " + ServerConfigFactory.INSTANCE.getSecondaryPort());
            WatchdogAgent.INSTANCE.reconnectSecondary();
        }, WatchdogAgent.INSTANCE.getProperties().reconnectDelay(), TimeUnit.SECONDS);
    }
}
