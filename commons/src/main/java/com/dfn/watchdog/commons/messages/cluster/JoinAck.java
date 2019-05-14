package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.View;

/**
 * Created by isurul on 6/2/2017.
 */
public class JoinAck implements JoinMessage {
    private View view;

    public JoinAck(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }
}
