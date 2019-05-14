package com.dfn.watchdog.commons;

import com.dfn.watchdog.commons.messages.monitoring.LinkStatus;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for View.
 */
public class ViewTest {
    @Test
    public void testView1() {
        View view = new View();
        Assert.assertNotNull(view.getServerState());
        Assert.assertTrue(view.getNodeMap().isEmpty());
        Assert.assertNotNull(view.getBroadcastView());
        Assert.assertNotNull(view.getLinks());
    }

    @Test
    public void testView2() {
        Map<NodeType, Integer> defaultNodes = new HashMap<>(3);
        defaultNodes.put(NodeType.GATEWAY, 2);
        defaultNodes.put(NodeType.OMS, 3);
        defaultNodes.put(NodeType.DFIX, 2);
        defaultNodes.put(NodeType.AURA, 0);
        View view = new View(defaultNodes);

        Assert.assertEquals(view.getNode(new Node((short) 1, NodeType.OMS, State.CLOSED)),
                new Node((short) 1, NodeType.OMS, State.CLOSED));
        Assert.assertFalse(view.getBroadcastView().getNodeMap().isEmpty());
        Assert.assertNotNull(view.getLinks());
    }

    @Test
    public void testGetAllNodes() {
        View view = getAView();
        Map<String, Node> flatNodeMap = view.getAllNodes();
        Assert.assertEquals(flatNodeMap.size(), 7);
    }

    @Test
    public void testGetAllNodes2() {
        View view = getAView();
        Map<String, Node> flatNodeMap = view.getAllNodes(NodeType.OMS);
        Assert.assertEquals(flatNodeMap.size(), 3);
    }

    @Test
    public void testGetAllNodesRaw() {
        View view = getAView();
        Map<Short, Node> flatNodeMap = view.getAllNodesRaw(NodeType.OMS);
        Assert.assertEquals(flatNodeMap.get(((Integer) 1).shortValue()).getName(), "OMS-1");
    }

    @Test
    public void testGetNode() {
        View view = getAView();
        Node node = new Node((short) 1, NodeType.OMS, State.CLOSED);
        Node nodeInView = view.getNode((short) 1, NodeType.OMS);
        Assert.assertEquals(node, nodeInView);
    }

    @Test
    public void testGetNode2() {
        View view = getAView();
        Node node = new Node((short) 1, NodeType.OMS, State.CLOSED);
        Node nodeInView = view.getNode("OMS-1");
        Assert.assertEquals(node, nodeInView);
    }

    @Test
    public void testGetNode3() {
        View view = getAView();
        Node node = new Node((short) 1, NodeType.OMS, State.CLOSED);
        Node nodeInView = view.getNode(node);
        Assert.assertEquals(node, nodeInView);
    }

    @Test
    public void testGetNode4() {
        View view = getAView();
        Node node = new Node((short) 1, NodeType.OMS, State.CLOSED);
        int id = ((EmptyChannel) view.getNode(node).getChannel()).getId();
        EmptyChannel channel = (EmptyChannel) node.getChannel();
        channel.setId(id);
        Node nodeInView = view.getNode(channel);
        Assert.assertEquals(node, nodeInView);
    }

    @Test
    public void testAddNode() {
        View view = new View();
        Node node = new Node((short) 1, NodeType.OMS, State.CLOSED);
        view.addNode(node);
        node = new Node((short) 2, NodeType.OMS, State.CLOSED);
        view.addNode(node);
        Assert.assertFalse(view.getAllNodes().isEmpty());
    }

    @Test
    public void testRefreshChannels() {
        View view = getAView();
        view = view.refreshChannels();
        Assert.assertTrue(view.getChannels().isEmpty());
    }

    @Test
    public void testGetBackup() {
        View view = getAView();
        Node node = view.getNode("OMS-2");
        node.changeState(State.CONNECTED);
        Node backupNode = view.getBackup(view.getNode("OMS-1"));
        Assert.assertEquals(node, backupNode);
    }

    @Test
    public void testPopulateLinks() {
        View view = getAView();
        view.populateLinks();
        Assert.assertFalse(view.getLinks().isEmpty());
        Assert.assertEquals(3, view.getLinks().get("GATEWAY-1").size());
    }

    @Test
    public void testRePopulateLinks() {
        View view = getAView();
        view.rePopulateLinks();
        Assert.assertFalse(view.getLinks().isEmpty());
        Assert.assertEquals(3, view.getLinks().get("GATEWAY-1").size());
    }

    @Test
    public void testAddToLinks() {
        View view = getAView();
        view.addToLinks(new LinkStatus("GATEWAY-1", "OMS-1", State.CLOSED));
        Assert.assertEquals(State.CLOSED, view.getLinks().get("GATEWAY-1").get("OMS-1"));
    }

    @Test
    public void testToString() {
        View view = getAView();
        Assert.assertNotNull(view.toString());
    }

    @Test
    public void testToString2() {
        View view = new View();
        Assert.assertNotNull(view.toString());
    }

    @Test
    public void testViewJson() throws Exception {
        View view = getAView();
        String viewString = view.toJsonMetric();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(viewString, Map.class);
        Assert.assertEquals(7, ((List) jsonMap.get("nodes")).size());
    }

    @Test
    public void testGetPrimary() {
        View view = getAView();
        short nodeId = view.getPrimary(NodeType.GATEWAY);
        Assert.assertEquals(0, nodeId);
    }

    private View getAView() {
        Map<NodeType, Integer> defaultNodes = new HashMap<>(3);
        defaultNodes.put(NodeType.GATEWAY, 2);
        defaultNodes.put(NodeType.OMS, 3);
        defaultNodes.put(NodeType.DFIX, 2);
        defaultNodes.put(NodeType.AURA, 0);
        return new View(defaultNodes);
    }

}
