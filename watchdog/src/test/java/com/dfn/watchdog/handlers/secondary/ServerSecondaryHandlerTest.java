package com.dfn.watchdog.handlers.secondary;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.messages.secondary.SecondaryJoin;
import com.dfn.watchdog.commons.messages.secondary.WDSecondary;
import com.dfn.watchdog.handlers.MockChannelHandlerContext;
import com.dfn.watchdog.util.WatchdogProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogServer.class})
@PowerMockIgnore("javax.management.*")
public class ServerSecondaryHandlerTest {
    private WatchdogServer watchdogServer;
    private List<Node> secondaryNodes = new ArrayList<>();
    private WatchdogProperties properties;
    private Node node;

    @Before
    public void setup() {
        node = new Node((short) 1, NodeType.GATEWAY, State.CONNECTED);
        watchdogServer = mock(WatchdogServer.class);
        properties = mock(WatchdogProperties.class);
        Whitebox.setInternalState(WatchdogServer.class, "INSTANCE", watchdogServer);

        doReturn(true).when(watchdogServer).isPrimary();
        doReturn(secondaryNodes).when(watchdogServer).getSecondaryNodes();
        doReturn(properties).when(watchdogServer).getProperties();
    }

    @Test
    public void channelRead0Test() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerSecondaryHandler serverSecondaryHandler = new ServerSecondaryHandler();
        serverSecondaryHandler.channelRead0(ctx, new SecondaryJoin(node, false));
        Assert.assertEquals(true, WatchdogServer.INSTANCE.getSecondaryNodes().contains(node));
    }

    @Test
    public void channelRead0Test1() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        ServerSecondaryHandler serverSecondaryHandler = new ServerSecondaryHandler();
        WDSecondary wdSecondary = new WDSecondary();
        wdSecondary.setNode(node);
        serverSecondaryHandler.channelRead0(ctx, wdSecondary);
    }
}
