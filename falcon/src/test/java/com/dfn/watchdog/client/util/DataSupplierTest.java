package com.dfn.watchdog.client.util;

import com.dfn.watchdog.client.WatchdogClient;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for DataHolder.
 */
public class DataSupplierTest {
    @Test
    public void testGetViewForTree() throws Exception {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));

        String jsonString = DataSupplier.getViewForTree(view);

        Assert.assertNotNull(jsonString);
    }

    @Test
    public void testGetViewForBlocks() throws Exception {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));
        WatchdogClient.INSTANCE.configure(null);

        Map<String, String> linkMap = new HashMap<>();
        String jsonString = DataSupplier.getViewForBlocks(view, linkMap);

        Assert.assertNotNull(jsonString);
    }
}
