package com.dfn.watchdog.commons.messages.commands;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.EventMessage;

/**
 * Instruction to a node.
 */
public class Command implements EventMessage {
    protected final short id;
    protected final NodeType type;

    /**
     * @param id node id
     * @param type node type
     */
    public Command(short id, NodeType type) {
        this.id = id;
        this.type = type;
    }

    public short getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }
}
