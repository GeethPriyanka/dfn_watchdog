package com.dfn.watchdog.commons.messages;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.monitoring.GatewayMetrics;
import com.dfn.watchdog.commons.util.Formatters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Unit tests for system metrics.
 */
public class GatewayMetricsTest {

    @Test
    public void testJsonStringConstruction() {
        GatewayMetrics gatewayMetrics = new GatewayMetrics((short) 1, NodeType.GATEWAY);
        Map<String, Object> jsonMap = Formatters.jsonStringToMap(gatewayMetrics.toJson());
        Assert.assertEquals(0, jsonMap.get("connected"));

        gatewayMetrics = new GatewayMetrics((short) 1, NodeType.GATEWAY, 5);
        jsonMap = Formatters.jsonStringToMap(gatewayMetrics.toJson());
        Assert.assertEquals(5, jsonMap.get("connected"));
    }
}