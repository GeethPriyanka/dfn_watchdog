package com.dfn.watchdog.commons;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for BroadcastView.
 */
public class BroadcastViewTest {

    @Test
    public void testBroadcastView() {
        View view = new View();
        BroadcastView broadcastView = new BroadcastView(view.getNodeMap());
        Assert.assertNotNull(broadcastView.getNodeMap());
    }

    @Test
    public void testToString() {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        BroadcastView broadcastView = new BroadcastView(view.getNodeMap());
        Assert.assertEquals("{Id: 1, Type: OMS, State: CONNECTED, IP: }\n", broadcastView.toString());
    }

}
