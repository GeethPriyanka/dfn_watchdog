package com.dfn.watchdog.commons.messages;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import com.dfn.watchdog.commons.util.Formatters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Unit tests for system metrics.
 */
public class JvmMetricsTest {

    @Test
    public void testJsonStringConstruction() {
        JvmMetrics jvmMetrics = new JvmMetrics((short) 1, NodeType.GATEWAY);
        jvmMetrics.setFreeMemory(20.5);
        Map<String, Object> jsonMap = Formatters.jsonStringToMap(jvmMetrics.toJson());

        JvmMetrics jvmMetrics2 = new JvmMetrics((short) 1, NodeType.GATEWAY);
        jvmMetrics2.copyFields(jvmMetrics);

        Assert.assertEquals("20.5", jsonMap.get("freeMemory"));
    }
}