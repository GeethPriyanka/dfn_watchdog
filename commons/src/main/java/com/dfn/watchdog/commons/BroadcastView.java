package com.dfn.watchdog.commons;

import com.dfn.watchdog.commons.messages.Streamable;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified representation of the View for frequent transmitting.
 */
public class BroadcastView implements Streamable {
    private int version;
    private Map<NodeType, Map<Short, Node>> nodeMap;

    public BroadcastView (Map<NodeType, Map<Short, Node>> nodeMap) {
        version = 1;
        this.nodeMap = nodeMap;
    }

    public Map<NodeType, Map<Short, Node>> getNodeMap() {
        return nodeMap;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    private Map<String, Node> getAllNodes() {
        Map<String, Node> flatNodeMap = new HashMap<>();
        for (Map<Short, Node> group : nodeMap.values()) {
            for (Node node : group.values()) {
                flatNodeMap.put(node.getName(), node);
            }
        }
        return flatNodeMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node node : getAllNodes().values()) {
            sb.append(node);
            sb.append("\n");
        }
        return sb.toString();
    }
}
