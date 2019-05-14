package com.dfn.watchdog.client;

import com.dfn.watchdog.commons.netty.ReconnectionHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Handles the reconnection.
 * <p>
 * On socket disconnection, will attempt to reconnect.
 * If there are no read events for readTimeout interval upstream handler will
 * fire an user event for reconnection.
 */
public class ClientReconnectionHandler extends ReconnectionHandler {
    private static final Logger logger = LogManager.getLogger(ClientReconnectionHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ClientReconnectionHandler() {
        super();
    }

    @Override
    public void handleDisconnection(final ChannelHandlerContext ctx) {
        WatchdogClient.INSTANCE.broadcastToWeb("{\"messageType\" : \"server_connect\", \"connected\" :false}");

        logger.warn("Sleeping for: " + 20 + " seconds");
        final EventLoopGroup eventLoopGroup = ctx.channel().eventLoop().parent();
        eventLoopGroup.schedule(() -> {
            logger.warn("Reconnecting to: " + WatchdogClient.INSTANCE.getProperties().serverIp());
            WatchdogClient.INSTANCE.reconnectClient();
        }, 20, TimeUnit.SECONDS);

        ctx.fireChannelUnregistered();
    }
}
