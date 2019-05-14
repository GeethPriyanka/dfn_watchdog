package com.dfn.watchdog.client;

import com.dfn.watchdog.client.util.ClientProperties;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.monitoring.SystemMetrics;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;

/**
 * CassandraConnection unit tests.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogClient.class})
@PowerMockIgnore("javax.management.*")
public class WebsocketHandlerTest {
    private static final ChannelHandlerContext context = mock(ChannelHandlerContext.class);

    private WebsocketHandler websocketHandler;
    private WatchdogClient watchdogClient;

    @Before
    public void setUp() throws Exception {
        when(context.channel()).thenReturn(new EmptyChannel());
        when(context.close()).thenReturn(null);
        websocketHandler = new WebsocketHandler();

        Map<NodeType, Integer> nodes = new HashMap<>(1);
        nodes.put(NodeType.GATEWAY, 1);
        nodes.put(NodeType.OMS, 1);
        nodes.put(NodeType.DFIX, 1);
        nodes.put(NodeType.AURA, 1);
        View view = new View(nodes);
        ClientProperties clientProperties = new ClientProperties();

        watchdogClient = PowerMockito.mock(WatchdogClient.class);
        Whitebox.setInternalState(WatchdogClient.class, "INSTANCE", watchdogClient);
        PowerMockito.doReturn(view).when(watchdogClient).getView();
        PowerMockito.doReturn(new SystemMetrics()).when(watchdogClient).getSystemMetrics();
        PowerMockito.doReturn(clientProperties).when(watchdogClient).getProperties();
    }

    @Test
    public void testChannelRead0() throws Exception {
        WebSocketFrame frame = new TextWebSocketFrame("hello watchdog");
        websocketHandler.channelRead0(context, frame);
    }

    @Test
    public void testChannelActive() throws Exception {
        websocketHandler.channelActive(context);
    }

    @Test
    public void testChannelInactive() throws Exception {
        websocketHandler.channelInactive(context);
    }

    @Test
    public void testExceptionCaught() throws Exception {
        websocketHandler.exceptionCaught(context, new Throwable("test throwable"));
    }

}