package com.dfn.watchdog.commons.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Common write listener.
 */
public class WriteListener implements ChannelFutureListener {
    private final Logger logger = LoggerFactory.getLogger(WriteListener.class);
    private final Object message;
    private final Channel channel;
    private final EventLoopGroup eventLoop;
    private final int readTimeout;
    private int retryCount;

    private static final int MAX_RETRY = 3;

    public WriteListener(Object message, Channel channel, EventLoopGroup eventLoop, int readTimeout) {
        this.message = message;
        this.channel = channel;
        this.eventLoop = eventLoop;
        this.readTimeout = readTimeout;
        retryCount = 0;
    }

    private WriteListener(Object message, Channel context,
                          EventLoopGroup eventLoop, int readTimeout, int retryCount) {
        this.message = message;
        this.channel = context;
        this.eventLoop = eventLoop;
        this.readTimeout = readTimeout;
        this.retryCount = retryCount;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            logger.warn("Failed to send the message: {} retries: {}", message, retryCount);
            if (retryCount < MAX_RETRY) {
                eventLoop.schedule(new Runnable() {
                    @Override
                    public void run() {
                        channel.writeAndFlush(message).addListener(
                                new WriteListener(message, channel, eventLoop, readTimeout, ++retryCount));
                    }
                }, readTimeout, TimeUnit.SECONDS);
            } else {
                logger.warn("Exceeded maximum retry count: {}", MAX_RETRY);
            }
        } else if (future.isCancelled()) {
            logger.warn("Message write was canceled by the user: {}", message);
        }
    }
}
