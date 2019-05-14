package com.dfn.watchdog.client.util;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Holds the history data of the components.
 */
public class DataHolder {
    private static final long HISTORY_PERIOD = 200;

    private Map<String, Queue<Double>> cpuUsage;

    public DataHolder(View view) {
        Map<String, Node> nodes = view.getAllNodes();

        cpuUsage = new HashMap<>(nodes.size());
        for (Node node : nodes.values()) {
            cpuUsage.put(node.getName(), new LinkedList<>());
        }
    }

    public Map<String, Queue<Double>> getCpuUsage() {
        return cpuUsage;
    }

    public void addToCpuUsage(String nodeName, double value) {
        Queue<Double> nodeCpu = cpuUsage.get(nodeName);
        if (nodeCpu == null) {
            nodeCpu = new LinkedList<>();
            cpuUsage.put(nodeName, nodeCpu);
        }
        if (nodeCpu.size() >= HISTORY_PERIOD) {
            nodeCpu.remove();
            nodeCpu.add(value);
        } else {
            nodeCpu.add(value);
        }
    }

    public void addToCpuUsage(NodeMetrics metric) {
        String nodeName = metric.getNodeType() + "-" + metric.getNodeId();
        Queue<Double> nodeCpu = cpuUsage.get(nodeName);
        if (nodeCpu == null) {
            nodeCpu = new LinkedList<>();
            cpuUsage.put(nodeName, nodeCpu);
        }
        if (nodeCpu.size() >= HISTORY_PERIOD) {
            nodeCpu.remove();
            nodeCpu.add(metric.getJvmMetrics().getSystemCpuUsage());
        } else {
            nodeCpu.add(metric.getJvmMetrics().getSystemCpuUsage());
        }
    }
}
