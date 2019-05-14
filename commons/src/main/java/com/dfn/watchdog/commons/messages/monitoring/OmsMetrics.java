package com.dfn.watchdog.commons.messages.monitoring;

import com.dfn.watchdog.commons.NodeType;

/**
 * Extend NodeMetrics class to contain OMS specific fields.
 */
public class OmsMetrics implements MonitoringMessage {
    private short nodeId;
    private NodeType nodeType;
    private long requestCount;

    public OmsMetrics(short id, NodeType type, int requestCount) {
        nodeId = id;
        nodeType = type;
        this.requestCount = requestCount;
    }

    public OmsMetrics(short id, NodeType type) {
        nodeId = id;
        nodeType = type;
        requestCount = 0;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"omsMetric\"");
        builder.append(",\"node\" :" + "\"" + nodeType + "-" + nodeId + "\"")
                .append(",\"requests\" :" + requestCount);

        return builder.append("}").toString();
    }
}
