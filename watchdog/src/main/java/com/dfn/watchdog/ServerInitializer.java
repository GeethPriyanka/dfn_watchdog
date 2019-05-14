package com.dfn.watchdog;

import com.dfn.watchdog.handlers.*;
import com.dfn.watchdog.handlers.monitoring.ServerMonitoringHandler;
import com.dfn.watchdog.handlers.secondary.ServerSecondaryHandler;
import com.dfn.watchdog.handlers.secondary.server.SecondaryServerHeartBeatHandler;
import com.dfn.watchdog.handlers.secondary.server.SecondaryServerJoinHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Configuration class of the netty pipeline.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final Logger logger = LogManager.getLogger(ServerInitializer.class);
    private final SslContext sslContext;

    /**
     * @param isSecured if SSL/TLS is enabled
     */
    ServerInitializer(Boolean isSecured) {
        sslContext = isSecured ? getNettySslContext() : null;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        if (sslContext != null) {
            pipeline.addLast("ssl", sslContext.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        pipeline.addLast(new ServerHeartbeatHandler());
        pipeline.addLast("server_route_handler", new ServerRouteHandler());
        pipeline.addLast(new ServerJoinHandler());
        pipeline.addLast(new SecondaryServerJoinHandler());
        pipeline.addLast(new SecondaryServerHeartBeatHandler());
        pipeline.addLast(new ServerChangeStateHandler());
        pipeline.addLast(new ServerMonitoringHandler());
        pipeline.addLast(new ServerSecondaryHandler());
        pipeline.addLast(new ServerHandlerMessage());
    }

    /**
     * Sample implementation of SslContext. Values are hardcoded - change them
     */
    private SslContext getNettySslContext() {
        SslContext nettySslContext = null;
        try (InputStream keyStoreStream = this.getClass().getClassLoader().getResourceAsStream("keystore.jks")) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreStream, "password".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "password".toCharArray());

            nettySslContext = SslContextBuilder.forServer(keyManagerFactory).build();

            SSLContext serverSslContext = SSLContext.getInstance("TLS");
            serverSslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        } catch (Exception e) {
            logger.error("SSL context error", e);
        }
        return nettySslContext;
    }
}
