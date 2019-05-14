package com.dfn.watchdog.util;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.cluster.ChangeView;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Listens for the message write fails.
 * <p>
 * If message writing failed, retry for few times until MAX_RETRY.
 */
public class ServerWriteListener implements ChannelFutureListener {
    private final Logger logger = LogManager.getLogger(ServerWriteListener.class);
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
    public ServerWriteListener(Object message, Channel channel) {
        this.message = message;
        this.channel = channel;
        retryCount = 0;
    }

    private ServerWriteListener(Object message, Channel channel, int retryCount) {
        this.message = message;
        this.channel = channel;
        this.retryCount = retryCount;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            Node node = WatchdogServer.INSTANCE.getView().getNode(channel);
            if (node == null || node.getState() != State.CONNECTED) {
                return;
            }

            node.getMetrics().addFailedMessage();
            logger.warn("Failed to send to " + node.getName() + ", message: " + message + ", retries: " + retryCount);
            if (retryCount < MAX_RETRY) {
                WatchdogServer.INSTANCE.getWorkerGroup().schedule(() -> {
                    channel.writeAndFlush(message).addListener(new ServerWriteListener(message, channel, ++retryCount));
                }, WatchdogServer.INSTANCE.getProperties().readTimeout(), TimeUnit.SECONDS);
            } else {
                View view = WatchdogServer.INSTANCE.getView();
                node.changeState(State.SUSPENDED).setChannel(new EmptyChannel());
                channel.close();
                view.refreshChannels();
                WatchdogServer.INSTANCE.broadcastMessage(new ChangeView(view));
                logger.warn("Exceeded maximum retry count: " + MAX_RETRY);
            }
        } else if (future.isCancelled()) {
            logger.warn("Message write was canceled by the user: " + message);
        }
    }
}
