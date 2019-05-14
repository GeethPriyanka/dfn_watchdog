package com.dfn.watchdog.handlers.secondary.server;

import com.dfn.watchdog.commons.messages.secondary.SecondaryJoinMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecondaryServerJoinHandler extends SimpleChannelInboundHandler<SecondaryJoinMessage> {
    private static final Logger logger = LogManager.getLogger(SecondaryServerJoinHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public SecondaryServerJoinHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SecondaryJoinMessage message) throws Exception {
        logger.info("Secondary Agent Join Message received: " + message);
        message.setAck(true);
        ctx.writeAndFlush(message);
    }
}