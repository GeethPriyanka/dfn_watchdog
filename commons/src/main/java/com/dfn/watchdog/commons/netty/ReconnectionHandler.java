package com.dfn.watchdog.commons.netty;

import com.dfn.watchdog.commons.messages.EventMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reconnecting to netty socket.
 */
public abstract class ReconnectionHandler extends SimpleChannelInboundHandler<EventMessage> {
    private static final Logger logger = LoggerFactory.getLogger(ReconnectionHandler.class);
    private long startTime = -1;

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ReconnectionHandler() {
        super(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }
        logger.info("Channel Active. " + "Uptime: " + (System.currentTimeMillis() - startTime) / 1000);
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, EventMessage msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        logger.warn("Disconnected from: " + ctx.channel().remoteAddress());
        logger.warn("Uptime: " + (System.currentTimeMillis() - startTime) / 1000);
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ReadTimeoutException) {
            logger.error("ReadTimeout Exception: ", cause);
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        handleDisconnection(ctx);
        ctx.fireChannelUnregistered();
    }

    public abstract void handleDisconnection(final ChannelHandlerContext ctx);
}
