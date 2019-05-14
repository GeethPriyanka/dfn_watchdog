package com.dfn.watchdog.commons.messages;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.monitoring.SystemMetrics;
import com.dfn.watchdog.commons.util.Formatters;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for system metrics.
 */
public class SystemMetricsTest {

    @Test
    public void testCalculate() {
        Map<NodeType, Integer> defaultNodes = new HashMap<>(3);
        defaultNodes.put(NodeType.GATEWAY, 2);
        defaultNodes.put(NodeType.OMS, 3);
        defaultNodes.put(NodeType.DFIX, 2);
        defaultNodes.put(NodeType.AURA, 0);
        View view = new View(defaultNodes);

        SystemMetrics systemMetrics = new SystemMetrics();
        systemMetrics.calculate(view);
        systemMetrics.setTps(123);
        systemMetrics.setClients(5);

        Map<String, Object> jsonMap = Formatters.jsonStringToMap(systemMetrics.toJson());
        Assert.assertEquals(0, jsonMap.get("disconnects"));
        Assert.assertEquals(5, jsonMap.get("clients"));
    }

}