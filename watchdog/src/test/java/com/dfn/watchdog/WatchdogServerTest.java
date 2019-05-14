package com.dfn.watchdog;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.exceptions.InvalidConfigurationError;
import com.dfn.watchdog.commons.messages.cluster.Heartbeat;
import io.netty.channel.ChannelFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for monitor handler.
 */
public class WatchdogServerTest {
    private static ChannelFuture channelFuture = null;

    @BeforeClass
    public static void setup() {
        channelFuture = WatchdogServer.INSTANCE.configure("./src/test/resources/watchdog-test1.yml").run();
    }

    @Test(expected = InvalidConfigurationError.class)
    public void testRunWithNoConfig() {
        WatchdogServer.INSTANCE.configure(" ").run();
    }

    @Test
    public void testConfigure() {
        Assert.assertNotNull(WatchdogServer.INSTANCE.getProperties());
        Assert.assertNotNull(WatchdogServer.INSTANCE.getView());
        Assert.assertNotNull(WatchdogServer.INSTANCE.getWorkerGroup());
        Assert.assertNotNull(WatchdogServer.INSTANCE.getActiveCustomers());
    }

    @Test
    public void runTest() {
        Assert.assertNotNull(channelFuture);
    }

    @Test
    public void testGetClientFromDb() {
        WatchdogServer.INSTANCE.setClientInDb(123, (short) 1);
        String clientRoute = WatchdogServer.INSTANCE.getClientFromDb(123);
        Assert.assertNotNull(clientRoute);
        channelFuture.cancel(true);
    }

    @Test
    public void testBroadcastMessage() {
        WatchdogServer.INSTANCE.broadcastMessage(new Heartbeat((short) 1, NodeType.OMS));
        channelFuture.cancel(true);
    }

    @Test
    public void testDeadNodeScanner() {
        WatchdogServer.INSTANCE.getDeadNodeFuture().cancel(false);
        View view = WatchdogServer.INSTANCE.getView();
        Node node = view.getNode("OMS-1");
        node.changeState(State.CONNECTED);
        node = view.getNode("GATEWAY-1");
        node.changeState(State.SUSPENDED);
        node = view.getNode("DFIX-1");
        node.changeState(State.CONNECTED);
        node.updateLastHeartbeat();

        WatchdogServer.INSTANCE.new DeadNodeScanner().run();

        Assert.assertEquals(State.SUSPENDED, view.getNode("OMS-1").getState());
        Assert.assertEquals(State.CLOSED, view.getNode("GATEWAY-1").getState());
        Assert.assertEquals(State.CONNECTED, view.getNode("DFIX-1").getState());
        channelFuture.cancel(true);
    }
}
