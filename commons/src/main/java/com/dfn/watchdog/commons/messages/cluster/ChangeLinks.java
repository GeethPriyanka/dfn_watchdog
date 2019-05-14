package com.dfn.watchdog.commons.messages.cluster;

import com.dfn.watchdog.commons.State;

import java.util.Map;

/**
 * Transfer message containing the system link status.
 */
public class ChangeLinks implements ChangeStateMessage {
    private Map<String, Map<String, State>> links;

    public ChangeLinks(Map<String, Map<String, State>> links) {
        this.links = links;
    }

    public Map<String, Map<String, State>> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Map<String, State>> links) {
        this.links = links;
    }
}
