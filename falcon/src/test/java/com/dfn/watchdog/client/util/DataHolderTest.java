package com.dfn.watchdog.client.util;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for DataHolder.
 */
public class DataHolderTest {
    @Test
    public void testAddCpuUsage() {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));

        DataHolder dataHolder = new DataHolder(view);
        dataHolder.addToCpuUsage(NodeType.OMS + "-1", 10.5);

        Assert.assertFalse(dataHolder.getCpuUsage().isEmpty());
    }
    @Test
    public void testAddCpuUsage2() {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));
        System.out.println("Check if this is running");

        DataHolder dataHolder = new DataHolder(view);
        NodeMetrics metric = new NodeMetrics((short) 1, NodeType.OMS);
        dataHolder.addToCpuUsage(metric);

        Assert.assertFalse(dataHolder.getCpuUsage().isEmpty());
    }
}
