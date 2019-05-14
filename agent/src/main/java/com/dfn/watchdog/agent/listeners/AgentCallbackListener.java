package com.dfn.watchdog.agent.listeners;

/**
 * Interface to the outside.
 * <p>
 * Marker interface.
 * Contains aggregate callbacks.
 */
public interface AgentCallbackListener extends StateChangeListener, AgentCallbacks, CommandListener {
}
