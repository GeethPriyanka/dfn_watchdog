package com.dfn.watchdog.commons.messages.cluster;

/**
 * Remove all mappings from gateway.
 */
public class PurgeMappings implements ChangeStateMessage {
    private final short nodeId;
    private String nodeType;

    public PurgeMappings(short nodeId) {
        this.nodeId = nodeId;
    }

    public PurgeMappings(short nodeId, String nodeType) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
    }

    public short getNodeId() {
        return nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }
}
