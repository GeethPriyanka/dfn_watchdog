package com.dfn.watchdog.agent.util;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Listens for the message write fails.
 * <p>
 * If message writing failed, retry for few times until MAX_RETRY.
 */
public class AgentWriteListener implements ChannelFutureListener {
    private final Logger logger = LoggerFactory.getLogger(AgentWriteListener.class);
    private final Object message;
    private final Channel channel;
    private int retryCount;

    private static final int MAX_RETRY = 3;

    /**
     * Create an new instance.
     *
     * @param message Message being written
     * @param channel Channel which message was written
     */
    public AgentWriteListener(Object message, Channel channel) {
        this.message = message;
        this.channel = channel;
        retryCount = 0;
    }

    private AgentWriteListener(Object message, Channel channel, int retryCount) {
        this.message = message;
        this.channel = channel;
        this.retryCount = retryCount;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            logger.warn("Failed to send the message: {} retries: {}", message, retryCount);
            if (retryCount < MAX_RETRY) {
                WatchdogAgent.INSTANCE.getEventLoop().schedule(() -> {
                    channel.writeAndFlush(message).addListener(new AgentWriteListener(message, channel, ++retryCount));
                }, WatchdogAgent.INSTANCE.getProperties().readTimeout(), TimeUnit.SECONDS);
            } else {
                logger.warn("Exceeded maximum retry count: {}, tearing down the connection", MAX_RETRY);
                Node me = WatchdogAgent.INSTANCE.getNode();
                me.changeState(State.SUSPENDED);
                me.getChannel().close();
                me.setChannel(new EmptyChannel());
            }
        } else if (future.isCancelled()) {
            logger.warn("Message write was canceled by the user: {}", message);
        }
    }
}
