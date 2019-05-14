package com.dfn.watchdog.agent;

import com.dfn.watchdog.agent.util.AgentProperties;
import com.dfn.watchdog.commons.BroadcastView;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * WatchdogAgent unit testing.
 */
public class WatchdogAgentTestSuite {
    @Test
    public void testSetNode() {
        WatchdogAgent.INSTANCE.setNode((short) 1, "GATEWAY");
        AgentProperties properties = WatchdogAgent.INSTANCE.getProperties();
        Assert.assertEquals(properties.agentId(), 1);
        Assert.assertEquals(properties.agentType(), NodeType.GATEWAY);
    }

    @Test
    public void testSetServer() {
        WatchdogAgent.INSTANCE.setServer("127.0.0.1", 7802);
        AgentProperties properties = WatchdogAgent.INSTANCE.getProperties();
        Assert.assertEquals(properties.serverIp(), "127.0.0.1");
        Assert.assertEquals(properties.port(), 7802);
    }

    @Test
    public void testSetTradeConnectivity() {
        WatchdogAgent.INSTANCE.setTradeConnectivity("127.0.0.1", 7802);
        AgentProperties properties = WatchdogAgent.INSTANCE.getProperties();
        Assert.assertEquals(properties.getTradeConnectIp(), "127.0.0.1");
        Assert.assertEquals(properties.getTradeConnectPort(), 7802);
    }

    @Test
    public void testInstallView() {
        Map<Short, Node> nodes = new HashMap<>();
        Map<NodeType, Map<Short, Node>> nodeMap = new HashMap<>();

        nodes.put((short) 1, new Node((short) 1, NodeType.OMS, State.CLOSED));
        nodeMap.put(NodeType.OMS, nodes);
        WatchdogAgent.INSTANCE.installView(new BroadcastView(nodeMap));

        Assert.assertNotNull(WatchdogAgent.INSTANCE.getView().getNode("OMS-1"));
    }

    @Test
    public void testPurgeClientRoutes() {
        WatchdogAgent.INSTANCE.purgeClientRoutes();
        Assert.assertTrue(WatchdogAgent.INSTANCE.getClientRouteMap().isEmpty());
    }

    @Test
    public void testPurgeClientRoutes2() {
        WatchdogAgent.INSTANCE.purgeClientRoutes((short) 1);
    }
}
