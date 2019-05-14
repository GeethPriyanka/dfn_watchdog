package com.dfn.watchdog.handlers;

import io.netty.channel.embedded.EmbeddedChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Embedded channel mock implementation.
 */
public class EmbeddedMockChannel extends EmbeddedChannel {
    @Override
    public SocketAddress remoteAddress() {
        return new InetSocketAddress(24525);
    }
}
