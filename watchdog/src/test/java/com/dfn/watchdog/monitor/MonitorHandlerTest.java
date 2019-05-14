package com.dfn.watchdog.monitor;

import com.dfn.watchdog.WatchdogEmbeddedChannel;
import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.client.ClientRouteRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogServer.class})
@PowerMockIgnore("javax.management.*")
public class MonitorHandlerTest {
    private WatchdogServer watchdogServer;
    private long clientId;
    private View view;

    @Before
    public void setup() {
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
    }

    @Test
    public void channelRead0Test() {
        ClientRouteRequest clientRouteRequest = new ClientRouteRequest(clientId);
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new MonitorHandler());
        channel.writeInbound(clientRouteRequest);

        EventMessage response = channel.readOutbound();
        assertNotNull(response);
        channel.finish();
    }
}
