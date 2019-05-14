package com.dfn.watchdog;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.client.ClientRouteRequest;
import com.dfn.watchdog.monitor.MonitorHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Unit tests for monitor handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerInitializerTest {
    @Mock
    private SocketChannel channel;
    private ChannelPipeline pipeline;

    @Before
    public void setup() {
       pipeline  = new WatchdogEmbeddedChannel().pipeline();
    }

    @Test
    public void testInitChannel() throws Exception {
        doReturn(pipeline).when(channel).pipeline();
        ServerInitializer serverInitializer = new ServerInitializer(false);
        serverInitializer.initChannel(channel);

        Assert.assertNotNull(pipeline.get("server_route_handler"));
        Assert.assertNull(pipeline.get("ssl"));
    }

    @Test
    public void testGetNettySslContext() throws Exception {
        doReturn(pipeline).when(channel).pipeline();
        ServerInitializer serverInitializer = new ServerInitializer(true);
        serverInitializer.initChannel(channel);

        Assert.assertNotNull(pipeline.get("ssl"));
    }
}
