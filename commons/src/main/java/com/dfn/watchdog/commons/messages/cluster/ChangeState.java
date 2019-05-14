package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.Node;

/**
 * Change state message. Sent when state of a node is changed.
 */
public class ChangeState implements ChangeStateMessage {
    Node node;

    public ChangeState(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
