package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.View;

/**
 * Transfer message containing the system view. Sent when view change is detected.
 */
public class ChangeView implements ChangeStateMessage {
    View view;

    public ChangeView(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
