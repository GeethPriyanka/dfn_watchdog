package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.Node;

/**
 * Created by isurul on 13/2/2017.
 */
public class BackupNotify implements ChangeStateMessage {
    Node node;

    public BackupNotify(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
