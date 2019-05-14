package com.dfn.watchdog.util;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Unit tests for monitor handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogServer.class})
@PowerMockIgnore("javax.management.*")
public class ServerWriteListenerTest {
    private WatchdogServer watchdogServer;
    private long clientId;
    private View view;

    private Channel channel;
    private String message;
    @Mock
    private ChannelFuture channelFuture;


    @Before
    public void setup() {
        channel = new EmbeddedChannel();
        message = "message to be sent";

        clientId = 123L;
        Map<NodeType, Integer> nodes = new HashMap<>(1);
        nodes.put(NodeType.GATEWAY, 1);
        nodes.put(NodeType.OMS, 1);
        nodes.put(NodeType.DFIX, 1);
        nodes.put(NodeType.AURA, 1);
        view = new View(nodes);

        watchdogServer = mock(WatchdogServer.class);
        Whitebox.setInternalState(WatchdogServer.class, "INSTANCE", watchdogServer);
        when(watchdogServer.getClientFromDb(Mockito.anyLong())).thenReturn("1");
        doReturn(view).when(watchdogServer).getView();

        doReturn(true).when(channelFuture).isDone();
        doReturn(true).when(channelFuture).isSuccess();
        doReturn(false).when(channelFuture).isCancelled();
    }

    @Test
    public void testOperationComplete() throws Exception {
        ServerWriteListener serverWriteListener = new ServerWriteListener(message, channel);
        serverWriteListener.operationComplete(channelFuture);

        /*ClientRouteRequest clientRouteRequest = new ClientRouteRequest(clientId);
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new MonitorHandler());
        channel.writeInbound(clientRouteRequest);

        EventMessage response = channel.readOutbound();
        assertNotNull(response);
        channel.finish();*/
    }
}
