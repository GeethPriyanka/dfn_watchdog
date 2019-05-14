package com.dfn.watchdog.handlers.secondary.server;

import com.dfn.watchdog.commons.messages.secondary.SecondaryHeartBeat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SecondaryServerHeartBeatHandler  extends SimpleChannelInboundHandler<SecondaryHeartBeat> {
    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public SecondaryServerHeartBeatHandler() {
        super(false);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SecondaryHeartBeat message) throws Exception {
        ctx.writeAndFlush(message);
    }
}