package com.dfn.watchdog.commons.messages.commands;

import com.dfn.watchdog.commons.NodeType;

/**
 * Instruct a node to restart.
 * <p>
 * Once this message is received, node should try to restart itself.
 */
public class StartEod extends Command {

    /**
     * @param id node id
     * @param type node type
     */
    public StartEod(short id, NodeType type) {
        super(id, type);
    }

    @Override
    public String toString() {
        return "StartEod: " + type + "-" + id;
    }
}
