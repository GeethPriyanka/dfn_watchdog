package com.dfn.watchdog.monitor;

import com.dfn.watchdog.WatchdogEmbeddedChannel;
import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.commands.Command;
import com.dfn.watchdog.commons.messages.commands.StartEod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Unit tests for monitor handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogServer.class})
@PowerMockIgnore("javax.management.*")
public class ServerCommandHandlerTest {
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
    public void channelRead0Test() {
        StartEod startEod = new StartEod((short) 1, NodeType.OMS);
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new ServerCommandHandler());
        channel.writeInbound(startEod);

        verify(WatchdogServer.INSTANCE, times(1)).startEodProcess();
        channel.finish();
    }

    @Test
    public void channelRead0CommandTest() {
        Command command = new Command((short) 1, NodeType.OMS);
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new ServerCommandHandler());
        channel.writeInbound(command);

        verify(WatchdogServer.INSTANCE, times(1)).getView();
        channel.finish();
    }
}
