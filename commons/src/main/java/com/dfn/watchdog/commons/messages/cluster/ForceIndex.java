package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.Node;

/**
 * Created by isurul on 13/3/2017.
 */
public class ForceIndex implements ForceIndexMessage {
    private long clientId;
    private Node node;

    public ForceIndex(long clientId, Node node) {
        this.clientId = clientId;
        this.node = node;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return clientId + " => " + node.getName();
    }
}
