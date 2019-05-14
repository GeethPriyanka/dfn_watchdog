package com.dfn.watchdog.commons.messages.inquery;

/**
 * Response for customer route message.
 */
public class RouteResponse implements RouteInquiry {
    private final long clientId;
    private final short next;
    private String nodeType;

    public RouteResponse(long clientId, short next) {
        this.clientId = clientId;
        this.next = next;
    }

    public RouteResponse(long clientId, short next, String nodeType) {
        this.clientId = clientId;
        this.next = next;
        this.setNodeType(nodeType);
    }

    public long getClientId() {
        return clientId;
    }

    public short getNext() {
        return next;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
}
