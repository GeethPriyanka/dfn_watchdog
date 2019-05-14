package com.dfn.watchdog.commons.messages.secondary;

import com.dfn.watchdog.commons.Node;

public class SecondaryJoin implements SecondaryMessage {
    private Node node;
    private boolean isPrimaryConnected;

    public SecondaryJoin(Node node, boolean isPrimaryConnected) {
        this.node = node;
        this.isPrimaryConnected = isPrimaryConnected;
    }

    public Node getNode() {
        return node;
    }

    public boolean isPrimaryConnected() {
        return isPrimaryConnected;
    }
}
