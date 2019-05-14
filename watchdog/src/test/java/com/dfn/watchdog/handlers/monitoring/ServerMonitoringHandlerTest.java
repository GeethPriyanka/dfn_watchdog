package com.dfn.watchdog.handlers.monitoring;

import com.dfn.watchdog.WatchdogEmbeddedChannel;
import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.monitoring.GatewayMetrics;
import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import com.dfn.watchdog.commons.messages.monitoring.LinkStatus;
import com.dfn.watchdog.commons.messages.monitoring.MonitoringMessage;
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
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Unit tests for server monitoring handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogServer.class})
@PowerMockIgnore("javax.management.*")
public class ServerMonitoringHandlerTest {
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
        when(watchdogServer.getClientFromDb(Mockito.anyLong())).thenReturn("1");
        doReturn(view).when(watchdogServer).getView();
    }

    @Test
    public void channelRead0JvmMeticsTest() {
        JvmMetrics jvmMetrics = new JvmMetrics((short) 1, NodeType.OMS);
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new ServerMonitoringHandler());
        channel.writeInbound(jvmMetrics);

        Mockito.verify(WatchdogServer.INSTANCE, times(1)).routeToMonitor(Mockito.any());
        channel.finish();
    }

    @Test
    public void channelRead0LinkStatusTest() {
        LinkStatus linkStatus = new LinkStatus("GATEWAY-1", "OMS-1", State.CONNECTED);
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new ServerMonitoringHandler());
        channel.writeInbound(linkStatus);

        Mockito.verify(WatchdogServer.INSTANCE, times(1)).routeToMonitor(Mockito.any());
        channel.finish();
    }

    @Test
    public void channelRead0GatewayMetricsTest() {
        GatewayMetrics gatewayMetrics = new GatewayMetrics((short) 1, NodeType.OMS);
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new ServerMonitoringHandler());
        channel.writeInbound(gatewayMetrics);

        Mockito.verify(WatchdogServer.INSTANCE, times(1)).routeToMonitor(Mockito.any());
        channel.finish();
    }

    @Test
    public void channelRead0ElseTest() {
        MonitoringMessage monitoringMessage = new MonitoringMessage() {
        };
        WatchdogEmbeddedChannel channel = new WatchdogEmbeddedChannel(new ServerMonitoringHandler());
        channel.writeInbound(monitoringMessage);

        Mockito.verify(WatchdogServer.INSTANCE, times(0)).routeToMonitor(Mockito.any());
        channel.finish();
    }
}
