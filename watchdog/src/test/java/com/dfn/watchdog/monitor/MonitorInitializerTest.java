package com.dfn.watchdog.monitor;

import com.dfn.watchdog.WatchdogEmbeddedChannel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.powermock.api.mockito.PowerMockito.doReturn;

/**
 * Unit tests for monitor handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class MonitorInitializerTest {
    @Mock
    private SocketChannel channel;
    private ChannelPipeline pipeline;

    @Before
    public void setup() {
        pipeline = new WatchdogEmbeddedChannel().pipeline();
    }

    @Test
    public void testInitChannel() throws Exception {
        doReturn(pipeline).when(channel).pipeline();
        MonitorInitializer monitorInitializer = new MonitorInitializer();
        monitorInitializer.initChannel(channel);

        Assert.assertNotNull(pipeline.get(ServerCommandHandler.class));
    }
}
