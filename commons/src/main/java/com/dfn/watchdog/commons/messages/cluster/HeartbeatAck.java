package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.BroadcastView;

/**
 * Created by isurul on 6/2/2017.
 */
public class HeartbeatAck implements HeartbeatMessage {
    BroadcastView view;

    public HeartbeatAck(BroadcastView view) {
        this.view = view;
    }

    public BroadcastView getView() {
        return view;
    }

    @Override
    public String toString() {
        return view.toString();
    }
}
