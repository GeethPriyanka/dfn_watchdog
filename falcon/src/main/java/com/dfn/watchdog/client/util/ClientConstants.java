package com.dfn.watchdog.client.util;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import com.dfn.watchdog.commons.util.Formatters;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Holder for the constants of the client
 */
public class ClientConstants {
    private ClientConstants() throws IllegalAccessError {
        throw new IllegalAccessError("Cannot instantiate class");
    }

    static final Map<String, String> tempNameMap = new HashMap<>();
    static final Map<String, String> treeColorMap = new HashMap<>();
    static final Map<NodeType, Integer> blockPositionMap = new EnumMap<>(NodeType.class);

    static {
        treeColorMap.put(State.CONNECTED.toString(), "#31ca34");
        treeColorMap.put(State.CONNECTING.toString(), "#31caba");
        treeColorMap.put(State.INITIALIZING.toString(), "#31caba");
        treeColorMap.put(State.SUSPENDED.toString(), "#cab531");
        treeColorMap.put(State.CLOSED.toString(), "#ca3131");

        blockPositionMap.put(NodeType.GATEWAY, 1);
        blockPositionMap.put(NodeType.OMS, 3);
        blockPositionMap.put(NodeType.AURA, 3);
        blockPositionMap.put(NodeType.DFIX, 5);
        blockPositionMap.put(NodeType.EXCHANGE, 7);

        tempNameMap.put("OMS", "EMS");
        tempNameMap.put("AURA", "BO");
        tempNameMap.put("GATEWAY", "GATEWAY");
        tempNameMap.put("DFIX", "DFIX");
    }

    public static String transformName(String name) {
        String[] temp = name.split("-");
        return tempNameMap.get(temp[0]) + "-" + temp[1];
    }

    public static String toJsonMetric(View view) {
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"view\"");
        builder.append(",\"nodes\" : [");
        int i = 0;
        for (Node node : view.getAllNodes().values()) {
            if (++i != 1) builder.append(",");
            builder.append("{");
            builder.append("\"nodeName\" :" + "\"" + transformName(node.getName()) + "\"");
            builder.append(",\"state\" :" + "\"" + node.getState() + "\"");
            builder.append(",\"ip\" :" + "\"" + node.getIpAddress() + "\"");
            if (node.getMetrics() != null) {
                JvmMetrics jvmMetrics = node.getMetrics().getJvmMetrics();
                builder.append(",\"metric\" :");
                builder.append("{");
                builder.append("\"node\" :" + "\"" + transformName(node.getName()) + "\"")
                        .append(",\"maxMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getMaxMemory()) + "\"")
                        .append(",\"usedMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getUsedMemory()) + "\"")
                        .append(",\"freeMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getFreeMemory()) + "\"")
                        .append(",\"threadCount\" :" + jvmMetrics.getThreadCount())
                        .append(",\"processCpuUsage\" :" + "\"" + Formatters.format(jvmMetrics.getProcessCpuUsage()) + "\"")
                        .append(",\"systemCpuUsage\" :" + "\"" + Formatters.format(jvmMetrics.getSystemCpuUsage()) + "\"")
                        .append(",\"lastUpdateTime\" :" + "\"" + Formatters.format(jvmMetrics.getLastUpdateTime()) + "\"");
                builder.append("}");
            }
            builder.append("}");
        }
        builder.append("]");

        return builder.append("}").toString();
    }

    public static String nodeMetricToJson(NodeMetrics nodeMetrics) {
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"metric\"");
        builder.append(",\"node\" :" + "\"" + transformName(nodeMetrics.getNodeType() + "-" + nodeMetrics.getNodeId()) + "\"")
                .append(",\"maxMemory\" :" + "\"" + Formatters.formatWithoutFractions(nodeMetrics.getJvmMetrics().getMaxMemory()) + "\"")
                .append(",\"usedMemory\" :" + "\"" + Formatters.formatWithoutFractions(nodeMetrics.getJvmMetrics().getUsedMemory()) + "\"")
                .append(",\"freeMemory\" :" + "\"" + Formatters.formatWithoutFractions(nodeMetrics.getJvmMetrics().getFreeMemory()) + "\"")
                .append(",\"threadCount\" :" + nodeMetrics.getJvmMetrics().getThreadCount())
                .append(",\"processCpuUsage\" :" + "\"" + Formatters.format(nodeMetrics.getJvmMetrics().getProcessCpuUsage()) + "\"")
                .append(",\"systemCpuUsage\" :" + "\"" + Formatters.format(nodeMetrics.getJvmMetrics().getSystemCpuUsage()) + "\"")
                .append(",\"disk\" :" + "\"" + Formatters.format(nodeMetrics.getJvmMetrics().getUsableDisk()) + "\"")
                .append(",\"lastUpdateTime\" :" + "\"" + Formatters.format(nodeMetrics.getJvmMetrics().getLastUpdateTime()) + "\"");

        return builder.append("}").toString();
    }
}
