package com.dfn.watchdog.commons.messages.secondary;

import com.dfn.watchdog.commons.Node;

public class WDSecondary implements SecondaryMessage {
    private Node node;

    public Node getNode() {
        return node;
    }

    public WDSecondary setNode(Node node) {
        this.node = node;
        return this;
    }
}
