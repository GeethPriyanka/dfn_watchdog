package com.dfn.watchdog.commons.messages.monitoring;

import com.dfn.watchdog.commons.NodeType;

/**
 * Extend NodeMetrics class to contain gateway specific fields.
 */
public class GatewayMetrics implements MonitoringMessage {
    private short nodeId;
    private NodeType nodeType;
    private int connectedClients;
    private long transactionCount;

    public GatewayMetrics(short id, NodeType type, int connectedClients) {
        nodeId = id;
        nodeType = type;
        this.connectedClients = connectedClients;
    }

    public GatewayMetrics(short id, NodeType type) {
        nodeId = id;
        nodeType = type;
        connectedClients = 0;
        transactionCount = 0;
    }

    public int getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(int connectedClients) {
        this.connectedClients = connectedClients;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public short getNodeId() {
        return nodeId;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"gatewayMetric\"");
        builder.append(",\"node\" :" + "\"" + nodeType + "-" + nodeId + "\"")
                .append(",\"connected\" :" + connectedClients)
                .append(",\"tps\" :" + transactionCount);

        return builder.append("}").toString();
    }
}
