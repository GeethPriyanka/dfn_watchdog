package com.dfn.watchdog.commons.messages;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import com.dfn.watchdog.commons.util.Formatters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Unit tests for system metrics.
 */
public class NodeMetricsTest {

    @Test
    public void testJsonStringConstruction() {
        NodeMetrics nodeMetrics = new NodeMetrics((short) 1, NodeType.GATEWAY);
        Map<String, Object> jsonMap = Formatters.jsonStringToMap(nodeMetrics.toJson());
        Assert.assertEquals("GATEWAY-1", jsonMap.get("node"));
    }

}