package com.dfn.watchdog;

import com.dfn.watchdog.handlers.secondary.agent.SecondaryAgentJoinHandler;
import com.dfn.watchdog.handlers.secondary.agent.SecondaryAgentReconnectionHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

public class SecondaryAgentInitializer extends ChannelInitializer<SocketChannel> {
    private final Logger logger = LogManager.getLogger(SecondaryAgentInitializer.class);
    private final SslContext sslContext;
    private SecondaryAgentReconnectionHandler secondaryAgentReconnectionHandler;

    /**
     * @param isSecured if SSL/TLS is enabled
     */
    SecondaryAgentInitializer(Boolean isSecured) {
        sslContext = isSecured ? getNettySslContext() : null;
        secondaryAgentReconnectionHandler = new SecondaryAgentReconnectionHandler();
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        if (sslContext != null) {
            pipeline.addLast("ssl", sslContext.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        pipeline.addLast(new ReadTimeoutHandler(WatchdogServer.INSTANCE.getProperties().readTimeout(), TimeUnit.SECONDS));
        pipeline.addLast(new SecondaryAgentJoinHandler());
        pipeline.addLast(secondaryAgentReconnectionHandler);
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
