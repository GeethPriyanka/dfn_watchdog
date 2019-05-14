package com.dfn.watchdog.agent;

import com.dfn.watchdog.agent.handlers.*;
import com.dfn.watchdog.agent.handlers.monitoring.AgentMonitoringHandler;
import com.dfn.watchdog.agent.util.ServerConfigFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class of the netty pipeline.
 */
public class AgentInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(WatchdogAgent.class);

    private final SslContext sslContext;

    private AgentReconnectionHandler agentReconnectionHandler;

    /**
     * @param isSecured if SSL/TLS is enabled
     */
    AgentInitializer(boolean isSecured) {
        sslContext = isSecured ? getNettySslContext() : null;
        agentReconnectionHandler = new AgentReconnectionHandler();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(ch.alloc(),
                    ServerConfigFactory.INSTANCE.getPrimaryIp(),
                    ServerConfigFactory.INSTANCE.getPrimaryPort()));
        }
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        pipeline.addLast(new ReadTimeoutHandler(WatchdogAgent.INSTANCE.getProperties().readTimeout(), TimeUnit.SECONDS));
        pipeline.addLast("AgentReconnectionHandler", agentReconnectionHandler);
        pipeline.addLast("AgentHeartbeatHandler", new AgentHeartbeatHandler());
        pipeline.addLast("AgentRouteHandler", new AgentRouteHandler());
        pipeline.addLast("AgentJoinHandler", new AgentJoinHandler());
        pipeline.addLast("AgentChangeStateHandler", new AgentChangeStateHandler());
        pipeline.addLast("AgentCommandHandler", new AgentCommandHandler());
        pipeline.addLast("AgentMonitoringHandler", new AgentMonitoringHandler());
        pipeline.addLast("AgentHandlerMessage", new AgentHandlerMessage());
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
