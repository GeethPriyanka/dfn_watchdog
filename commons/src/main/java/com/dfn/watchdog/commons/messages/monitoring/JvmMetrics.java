package com.dfn.watchdog.commons.messages.monitoring;

import com.dfn.watchdog.commons.NodeType;

import java.text.DecimalFormat;

/**
 * Holder for component's system metrics.
 */
public class JvmMetrics implements MonitoringMessage {
    private short nodeId;
    private NodeType nodeType;

    private double maxMemory;
    private double usedMemory;
    private double freeMemory;
    private long threadCount;
    private double processCpuUsage;
    private double systemCpuUsage;
    private double usableDisk;

    private long lastUpdateTime;

    public JvmMetrics(short id, NodeType type) {
        this.nodeId = id;
        this.nodeType = type;
        reset();
    }

    public void copyFields(JvmMetrics metrics) {
        maxMemory = metrics.maxMemory;
        usedMemory = metrics.usedMemory;
        usedMemory = metrics.freeMemory;
        threadCount = metrics.threadCount;
        processCpuUsage = metrics.processCpuUsage;
        systemCpuUsage = metrics.systemCpuUsage;
        usableDisk = metrics.usableDisk;
    }

    public void reset() {
        maxMemory = 0;
        freeMemory = 0;
        usedMemory = 0;
        threadCount = 0;
        processCpuUsage = 0;
        systemCpuUsage = 0;
        lastUpdateTime = 0;
        usableDisk = 0;
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

    public double getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(double maxMemory) {
        this.maxMemory = maxMemory;
    }

    public double getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(double freeMemory) {
        this.freeMemory = freeMemory;
    }

    public double getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(double usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(long threadCount) {
        this.threadCount = threadCount;
    }

    public double getProcessCpuUsage() {
        return processCpuUsage;
    }

    public void setProcessCpuUsage(double processCpuUsage) {
        this.processCpuUsage = processCpuUsage;
    }

    public double getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(double systemCpuUsage) {
        this.systemCpuUsage = systemCpuUsage;
    }

    public double getUsableDisk() {
        return usableDisk;
    }

    public void setUsableDisk(double usableDisk) {
        this.usableDisk = usableDisk;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String toJson() {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"metric\"");
        builder.append(",\"node\" :" + "\"" + nodeType + "-" + nodeId + "\"")
                .append(",\"maxMemory\" :" + "\"" + decimalFormat.format(maxMemory) + "\"")
                .append(",\"usedMemory\" :" + "\"" + decimalFormat.format(usedMemory) + "\"")
                .append(",\"freeMemory\" :" + "\"" + decimalFormat.format(freeMemory) + "\"")
                .append(",\"threadCount\" :" + threadCount)
                .append(",\"processCpuUsage\" :" + "\"" + decimalFormat.format(processCpuUsage) + "\"")
                .append(",\"systemCpuUsage\" :" + "\"" + decimalFormat.format(systemCpuUsage) + "\"")
                .append(",\"disk\" :" + "\"" + decimalFormat.format(usableDisk) + "\"");

        return builder.append("}").toString();
    }
}