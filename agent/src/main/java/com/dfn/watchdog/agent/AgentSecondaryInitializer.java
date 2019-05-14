package com.dfn.watchdog.agent;


import com.dfn.watchdog.agent.handlers.secondary.AgentSecondaryHandler;
import com.dfn.watchdog.agent.handlers.secondary.AgentSecondaryReconnectionHandler;
import com.dfn.watchdog.agent.util.ServerConfigFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class AgentSecondaryInitializer  extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(WatchdogAgent.class);

    private final SslContext sslContext;

    /**
     * @param isSecured if SSL/TLS is enabled
     */
    AgentSecondaryInitializer(boolean isSecured) {
        sslContext = isSecured ? getNettySslContext() : null;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(ch.alloc(),
                    ServerConfigFactory.INSTANCE.getSecondaryIp(),
                    ServerConfigFactory.INSTANCE.getSecondaryPort()));
        }
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        pipeline.addLast(new AgentSecondaryReconnectionHandler());
        pipeline.addLast(new AgentSecondaryHandler());
    }

    /**
     * Sample implementation of SslContext. Values are hardcoded - change them
     */
    private SslContext getNettySslContext() {
        SslContext serverSslContext = null;
        try (FileInputStream trustStoreStream = new FileInputStream("truststore.jks")) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStoreStream, "password".toCharArray());
            TrustManagerFactory trustStoreManger = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustStoreManger.init(trustStore);

            serverSslContext = SslContextBuilder.forClient().trustManager(trustStoreManger).build();
        } catch (Exception e) {
            logger.error("SSL context error", e);
        }
        return serverSslContext;
    }
}