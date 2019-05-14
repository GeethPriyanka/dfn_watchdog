package com.dfn.watchdog.commons.messages.commands;

import com.dfn.watchdog.commons.NodeType;

/**
 * Instruct a node to shutdown.
 * <p>
 * Once this message is received, node should try to gracefully shutdown.
 */
public class Shutdown extends Command {

    /**
     * @param id node id
     * @param type node type
     */
    public Shutdown(short id, NodeType type) {
        super(id, type);
    }

    @Override
    public String toString() {
        return "Shutdown: " + type + "-" + id;
    }
}
