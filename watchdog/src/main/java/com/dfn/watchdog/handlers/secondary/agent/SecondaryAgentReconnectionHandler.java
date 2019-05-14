package com.dfn.watchdog.handlers.secondary.agent;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.netty.ReconnectionHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class SecondaryAgentReconnectionHandler  extends ReconnectionHandler {
    private static final Logger logger = LogManager.getLogger(SecondaryAgentReconnectionHandler.class);
    private static final int RECONNECTS_BEFORE_CLOSED = 3;
    private long reconnectionAttempts = 0;

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public SecondaryAgentReconnectionHandler() {
        super();
    }

    @Override
    public void handleDisconnection(final ChannelHandlerContext ctx) {
        if (reconnectionAttempts > RECONNECTS_BEFORE_CLOSED) {
            WatchdogServer.INSTANCE.setSecondaryDisconnected(true);
            reconnectionAttempts = 0;
        }
        reconnectionAttempts++;

        final EventLoopGroup eventLoopGroup = ctx.channel().eventLoop().parent();
        eventLoopGroup.schedule(() -> {
            logger.warn("Reconnecting to: " + WatchdogServer.INSTANCE.getProperties().getSecondaryServerIp()
                    + " : " + WatchdogServer.INSTANCE.getProperties().getSecondaryServerPort());
            WatchdogServer.INSTANCE.reconnectSecondary();
        }, WatchdogServer.INSTANCE.getProperties().reconnectDelay(), TimeUnit.SECONDS);
    }
}
