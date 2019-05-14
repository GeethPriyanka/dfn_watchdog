package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;

/**
 * Created by isurul on 3/2/2017.
 */
public class Heartbeat implements HeartbeatMessage {
    short id;
    NodeType type;

    public Heartbeat(short id, NodeType type) {
        this.id = id;
        this.type = type;
    }

    public Heartbeat(Node node) {
        id = node.getId();
        type = node.getType();
    }

    public short getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + "-" + id;
    }
}
