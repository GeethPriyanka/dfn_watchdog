package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.cluster.Join;
import com.dfn.watchdog.util.WatchdogProperties;
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
public class ServerJoinHandlerTest {
    private WatchdogServer watchdogServer;
    private View view;
    private WatchdogProperties properties;

    @Before
    public void setup() {
        Map<NodeType, Integer> nodes = new HashMap<>(1);
        nodes.put(NodeType.GATEWAY, 1);
        nodes.put(NodeType.OMS, 1);
        nodes.put(NodeType.DFIX, 1);
        nodes.put(NodeType.AURA, 1);
        view = new View(nodes);
        properties = mock(WatchdogProperties.class);

        watchdogServer = mock(WatchdogServer.class);
        Whitebox.setInternalState(WatchdogServer.class, "INSTANCE", watchdogServer);
        doReturn(view).when(watchdogServer).getView();
        doReturn(properties).when(watchdogServer).getProperties();
    }

    @Test
    public void channelRead0Test() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerJoinHandler serverJoinHandler = new ServerJoinHandler();
        doReturn(true).when(watchdogServer).isPrimary();
        serverJoinHandler.channelRead0(ctx, new Join(new Node((short) 1, NodeType.GATEWAY, State.CLOSED)));
    }

    @Test
    public void channelRead0Test1() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerJoinHandler serverJoinHandler = new ServerJoinHandler();
        doReturn(false).when(watchdogServer).isPrimary();
        serverJoinHandler.channelRead0(ctx, new Join(new Node((short) 1, NodeType.GATEWAY, State.CLOSED)));
    }
}
