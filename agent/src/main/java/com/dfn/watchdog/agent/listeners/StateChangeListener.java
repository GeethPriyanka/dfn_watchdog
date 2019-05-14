package com.dfn.watchdog.agent.listeners;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.State;

/**
 * Publishes state changes to outside.
 */
public interface StateChangeListener {
    boolean initializing(State fromState);

    boolean connecting(State fromState);

    boolean connected(State fromState);

    boolean suspended(State fromState);

    boolean leaving(State fromState);

    boolean failed(State fromState);

    boolean closed(State fromState);

    boolean backupRecovering(Node node);

    boolean backupRecovered(Node node);

    boolean backupRecoveryFailed(Node node);
}
