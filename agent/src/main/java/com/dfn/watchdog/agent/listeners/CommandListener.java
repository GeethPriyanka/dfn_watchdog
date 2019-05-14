package com.dfn.watchdog.agent.listeners;

import com.dfn.watchdog.commons.View;

/**
 * Interface for the commands sent by the server.
 * <p>
 * Agent should take necessary actions.
 */
public interface CommandListener {
    void shutdown();
    void restart();
    void startEod(short nodeId);
    void updateConfiguration(View view);
}
