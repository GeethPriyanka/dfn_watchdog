package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.inquery.RouteRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Unit tests for monitor handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogServer.class})
@PowerMockIgnore("javax.management.*")
public class ServerRouteHandlerTest {
    private WatchdogServer watchdogServer;
    private View view;

    @Before
    public void setup() {
        Map<NodeType, Integer> nodes = new HashMap<>(1);
        nodes.put(NodeType.GATEWAY, 1);
        nodes.put(NodeType.OMS, 1);
        nodes.put(NodeType.DFIX, 1);
        nodes.put(NodeType.AURA, 1);
        view = new View(nodes);

        watchdogServer = mock(WatchdogServer.class);
        Whitebox.setInternalState(WatchdogServer.class, "INSTANCE", watchdogServer);
        doReturn(view).when(watchdogServer).getView();
    }

    @Test
    public void channelRead0Test() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerRouteHandler serverRouteHandler = new ServerRouteHandler();
        try {
            serverRouteHandler.channelRead0(ctx, new RouteRequest(1234L));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
