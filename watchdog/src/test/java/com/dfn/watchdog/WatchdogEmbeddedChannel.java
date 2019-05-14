package com.dfn.watchdog;

import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Extend netty EmbeddedChannel for unit tests.
 */
public class WatchdogEmbeddedChannel extends EmbeddedChannel {

    public WatchdogEmbeddedChannel(ChannelHandler... handlers) {
        super(handlers);
    }

    @Override
    public SocketAddress remoteAddress() {
        return new InetSocketAddress(8080);
    }

}
