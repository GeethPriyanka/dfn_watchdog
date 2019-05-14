package com.dfn.watchdog.client;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for WatchdogClient.
 */
public class WatchdogClientTestSuite {
    @Test
    public void testInstallView() {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));
        WatchdogClient.INSTANCE.installView(view);

        Assert.assertNotNull(WatchdogClient.INSTANCE.getView());
    }

    @Test
    public void testBroadcastToWeb() {
        WatchdogClient.INSTANCE.addWebSession(new EmptyChannel());
        WatchdogClient.INSTANCE.removeWebSession(new EmptyChannel());
        WatchdogClient.INSTANCE.broadcastToWeb("We are landing to moon, without people");
        WatchdogClient.INSTANCE.addWebSession(new EmptyChannel());
        WatchdogClient.INSTANCE.broadcastToWeb("We are landing to moon");
        Assert.assertNotNull(WatchdogClient.INSTANCE.getView());
    }
}
