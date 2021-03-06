package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.cluster.BackupReady;
import com.dfn.watchdog.commons.messages.cluster.ChangeState;
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
public class ServerChangeStateHanderTest {
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
        ServerChangeStateHandler stateHandler = new ServerChangeStateHandler();
        stateHandler.channelRead0(ctx, new ChangeState(new Node((short) 1, NodeType.OMS, State.CLOSED)));
    }

    @Test
    public void channelRead0Test2() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerChangeStateHandler stateHandler = new ServerChangeStateHandler();
        stateHandler.channelRead0(ctx, new BackupReady(new Node((short) 1, NodeType.OMS, State.CLOSED)));
    }

    @Test
    public void channelInactiveTest() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerChangeStateHandler stateHandler = new ServerChangeStateHandler();
        stateHandler.channelInactive(ctx);
    }

    @Test
    public void exceptionCaughtTest() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerChangeStateHandler stateHandler = new ServerChangeStateHandler();
        stateHandler.exceptionCaught(ctx, new Throwable("Test exception caught"));
    }
}
