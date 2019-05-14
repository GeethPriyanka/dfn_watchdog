package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.agent.util.ServerConfigFactory;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.netty.ReconnectionHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Handles the reconnection.
 * <p>
 * On socket disconnection, will attempt to reconnect.
 * If there are no read events for readTimeout interval upstream handler will
 * fire an user event for reconnection.
 */
@ChannelHandler.Sharable
public class AgentReconnectionHandler extends ReconnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AgentReconnectionHandler.class);
    private static final int RECONNECTS_BEFORE_CLOSED = 3;
    private long reconnectionAttempts = 0;

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentReconnectionHandler() {
        super();
    }

    @Override
    public void handleDisconnection(final ChannelHandlerContext ctx) {
        final State currentState = WatchdogAgent.INSTANCE.getNode().getState();

        try {
            switch (WatchdogAgent.INSTANCE.getNode().getState()) {
                case CONNECTED:
                    WatchdogAgent.INSTANCE.getNode().changeState(State.SUSPENDED);
                    WatchdogAgent.INSTANCE.getListener().suspended(currentState);
                    reconnectionAttempts = 1;
                    break;
                case SUSPENDED:
                    if (reconnectionAttempts > RECONNECTS_BEFORE_CLOSED) {
                        WatchdogAgent.INSTANCE.getNode().changeState(State.CLOSED);
                        WatchdogAgent.INSTANCE.getListener().leaving(currentState);
                        WatchdogAgent.INSTANCE.broadcastSecondaryConnect();
                    }
                    reconnectionAttempts++;
                    break;
                default:
                    if (reconnectionAttempts > RECONNECTS_BEFORE_CLOSED) {
                        // if primary server is down when agent initially try to connect
                        WatchdogAgent.INSTANCE.broadcastSecondaryConnect();
                    }
                    reconnectionAttempts++;
                    break;
            }
        } catch (Exception e) {
            logger.error("handleDisconnection() -> Exception on changing state", e);
        }

        logger.warn("Sleeping for: " + WatchdogAgent.INSTANCE.getProperties().reconnectDelay() + " seconds");
        final EventLoopGroup eventLoopGroup = ctx.channel().eventLoop().parent();
        eventLoopGroup.schedule(() -> {
            logger.warn("Reconnecting to: " + ServerConfigFactory.INSTANCE.getPrimaryIp() + " : " + ServerConfigFactory.INSTANCE.getPrimaryPort());
            WatchdogAgent.INSTANCE.reconnectAgent();
        }, WatchdogAgent.INSTANCE.getProperties().reconnectDelay(), TimeUnit.SECONDS);
    }
}
