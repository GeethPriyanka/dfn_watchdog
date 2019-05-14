package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.Node;

/**
 * Request for joining the cluster.
 */
public class Join implements JoinMessage {
    private Node node;

    public Join(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
