package com.dfn.watchdog.util;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.cluster.ChangeView;

/**
 * Initialize the system.
 */
public class SystemInitializer {
    public void init() {
        View view = WatchdogServer.INSTANCE.getView();
        view.setServerState(State.INITIALIZING);
        WatchdogServer.INSTANCE.broadcastMessage(new ChangeView(view));
    }
}
