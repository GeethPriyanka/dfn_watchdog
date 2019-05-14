package com.dfn.watchdog.commons.messages.monitoring;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.util.Formatters;

/**
 * Holder for component's system metrics.
 */
public class NodeMetrics implements MonitoringMessage {
    private short nodeId;
    private NodeType nodeType;

    private long failedMessages;
    private long disconnects;
    private State prevState;
    private long stateChangeTime;

    private JvmMetrics jvmMetrics;

    private long lastUpdateTime;

    public NodeMetrics(short id, NodeType type) {
        this.nodeId = id;
        this.nodeType = type;
        reset();
    }

    public void copyFields(NodeMetrics metrics) {
        //todo add if we want or remove
    }

    public void reset() {
        failedMessages = 0;
        disconnects = 0;
        prevState = State.CONNECTED;
        stateChangeTime = System.currentTimeMillis();

        jvmMetrics = new JvmMetrics(nodeId, nodeType);

        lastUpdateTime = stateChangeTime;
    }

    public void addFailedMessage() {
        failedMessages++;
    }

    public void addDisconnect() {
        disconnects++;
    }

    public short getNodeId() {
        return nodeId;
    }

    public void setNodeId(short nodeId) {
        this.nodeId = nodeId;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public long getFailedMessages() {
        return failedMessages;
    }

    public void setFailedMessages(long failedMessages) {
        this.failedMessages = failedMessages;
    }

    public long getDisconnects() {
        return disconnects;
    }

    public void setDisconnects(long disconnects) {
        this.disconnects = disconnects;
    }

    public State getPrevState() {
        return prevState;
    }

    public void setPrevState(State prevState) {
        this.prevState = prevState;
    }

    public long getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(long stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    public JvmMetrics getJvmMetrics() {
        return jvmMetrics;
    }

    public void setJvmMetrics(JvmMetrics jvmMetrics) {
        this.jvmMetrics = jvmMetrics;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"metric\"");
        builder.append(",\"node\" :" + "\"" + nodeType + "-" + nodeId + "\"")
                .append(",\"maxMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getMaxMemory()) + "\"")
                .append(",\"usedMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getUsedMemory()) + "\"")
                .append(",\"freeMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getFreeMemory()) + "\"")
                .append(",\"threadCount\" :" + jvmMetrics.getThreadCount())
                .append(",\"processCpuUsage\" :" + "\"" + Formatters.format(jvmMetrics.getProcessCpuUsage()) + "\"")
                .append(",\"systemCpuUsage\" :" + "\"" + Formatters.format(jvmMetrics.getSystemCpuUsage()) + "\"")
                .append(",\"disk\" :" + "\"" + Formatters.format(jvmMetrics.getUsableDisk()) + "\"")
                .append(",\"lastUpdateTime\" :" + "\"" + Formatters.format(jvmMetrics.getLastUpdateTime()) + "\"");

        return builder.append("}").toString();
    }
}