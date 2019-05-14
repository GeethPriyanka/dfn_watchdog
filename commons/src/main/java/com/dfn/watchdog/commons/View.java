package com.dfn.watchdog.commons;

import com.dfn.watchdog.commons.messages.Streamable;
import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import com.dfn.watchdog.commons.messages.monitoring.LinkStatus;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import com.dfn.watchdog.commons.util.Formatters;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cluster view.
 * <p>
 * Holds all the nodes in the cluster, default sizes and connected channels.
 */
public class View implements Streamable {
    private static final transient Logger logger = LoggerFactory.getLogger(View.class);
    private int version;
    private final Map<NodeType, Map<Short, Node>> nodeMap;
    private Map<NodeType, Integer> defaultNodes;
    private State serverState;

    private transient List<Channel> channels;
    private transient Channel monitorChannel;
    private final BroadcastView broadcastView;
    private transient Map<String, Map<String, State>> links;

    public View() {
        version = 1;
        serverState = State.CONNECTED;
        nodeMap = new EnumMap<>(NodeType.class);
        channels = new ArrayList<>();
        broadcastView = new BroadcastView(nodeMap);
        links = new HashMap<>();
    }

    public View(Map<NodeType, Integer> defaultNodes) {
        version = 1;
        serverState = State.CONNECTED;
        nodeMap = new EnumMap<>(NodeType.class);
        this.defaultNodes = defaultNodes;
        for (Map.Entry<NodeType, Integer> e : defaultNodes.entrySet()) {
            Map<Short, Node> nodes = new HashMap<>(e.getValue());
            for (short i = 1; i <= e.getValue(); i++) {
                nodes.put(i, new Node(i, e.getKey(), State.CLOSED));
            }
            nodeMap.put(e.getKey(), nodes);
        }
        channels = new ArrayList<>();
        monitorChannel = null;
        broadcastView = new BroadcastView(nodeMap);
        links = new HashMap<>();
        populateLinks();
    }

    public Map<NodeType, Map<Short, Node>> getNodeMap() {
        return nodeMap;
    }

    public Map<String, Node> getAllNodes() {
        Map<String, Node> flatNodeMap = new HashMap<>();
        for (Map<Short, Node> group : nodeMap.values()) {
            for (Node node : group.values()) {
                flatNodeMap.put(node.getName(), node);
            }
        }
        return flatNodeMap;
    }

    public Map<String, Node> getAllNodes(NodeType type) {
        Map<String, Node> flatNodeMap = new HashMap<>();
        for (Node node : nodeMap.get(type).values()) {
            flatNodeMap.put(node.getName(), node);
        }
        return flatNodeMap;
    }

    public Map<Short, Node> getAllNodesRaw(NodeType type) {
        return nodeMap.get(type);
    }

    public Node getNode(short id, NodeType type) {
        Node node = nodeMap.get(type).get(id);
        if (node == null) {
            logger.warn("Request received for empty node: {}-{}", type, id);
            logger.warn("Current view: {}", this);
        }
        return node;
    }

    public Node getNode(String name) {
        String[] s = name.split("-");
        return nodeMap.get(NodeType.valueOf(s[0])).get(Short.valueOf(s[1]));
    }

    public Node getNode(Node node) {
        return nodeMap.get(node.getType()).get(node.getId());
    }

    public Node getNode(Channel channel) {
        for (Map<Short, Node> group : nodeMap.values()) {
            for (Node node : group.values()) {
                if (node.getChannel().equals(channel)) {
                    return node;
                }
            }
        }
        return null;
    }

    public boolean addNode(Node node) {
        boolean success = true;
        node.updateLastHeartbeat();
        if (nodeMap.containsKey(node.getType())) {
            Map<Short, Node> flatNodeMap = nodeMap.get(node.getType());
            if (!flatNodeMap.containsKey(node.getId())) {
                node.setMetrics(new NodeMetrics(node.getId(), node.getType()));
                flatNodeMap.put(node.getId(), node);
            }

            Node currentNode = flatNodeMap.get(node.getId());
            if (!flatNodeMap.get(node.getId()).getChannel().equals(node.getChannel())) {
                if (!(flatNodeMap.get(node.getId()).getState() == State.CLOSED ||
                        flatNodeMap.get(node.getId()).getState() == State.SUSPENDED)) {
                    success = false;
                } else {
                    currentNode.getChannel().close();
                    flatNodeMap.put(node.getId(), currentNode.copyValues(node));
                }
            } else {
                flatNodeMap.put(node.getId(), currentNode.copyValues(node));
            }
        } else {
            Map<Short, Node> flatNodeMap = new HashMap<>();
            nodeMap.put(node.getType(), flatNodeMap);
            node.setMetrics(new NodeMetrics(node.getId(), node.getType()));
            flatNodeMap.put(node.getId(), node);
        }
        if (success) {
            incrementVersion();
            rePopulateLinks();//links will not be updated for nodes not in default view
            logger.info("New node added to the view: {}", node);
        }

        return success;
    }

    public void incrementVersion() {
        broadcastView.setVersion(++version);
        logger.info("View version update to: {}", version);
    }

    public void setVersion(int version) {
        this.version = version;
        broadcastView.setVersion(version);
        logger.info("View version update to: {}", version);
    }

    public State getServerState() {
        return serverState;
    }

    public void setServerState(State serverState) {
        this.serverState = serverState;
    }

    public View refreshChannels() {
        channels = getAllNodes().values().stream()
                .filter(n -> n.getChannel() != null && !(n.getChannel() instanceof EmptyChannel))
                .map(Node::getChannel)
                .collect(Collectors.toList());
        return this;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public int getVersion() {
        return version;
    }

    public int getDefaultSize(NodeType type) {
        return defaultNodes.get(type);
    }

    public Channel getMonitorChannel() {
        return monitorChannel;
    }

    public void setMonitorChannel(Channel channel) {
        monitorChannel = channel;
    }

    public BroadcastView getBroadcastView() {
        return broadcastView;
    }

    public Node getBackup(Node node) {
        Node nextNode = null;
        short index = node.getId();

        short i = index;
        Map<Short, Node> flatNodeMap = getAllNodesRaw(NodeType.OMS);
        do {
            if (i >= flatNodeMap.size()) {
                i = 1;
            } else {
                i++;
            }
            while (!flatNodeMap.containsKey(i)) {//if node indexes are not continuous
                i++;
            }
            if (flatNodeMap.get(i).getState() == State.CONNECTED) {
                nextNode = flatNodeMap.get(i);
                break;
            }
        } while (i != index);

        return nextNode;
    }

    public void populateLinks() {
        if (links.isEmpty()) {
            Map<String, Node> gatewayMap = getAllNodes(NodeType.GATEWAY);
            Map<String, Node> omsMap = getAllNodes(NodeType.OMS);
            Map<String, Node> dfixMap = getAllNodes(NodeType.DFIX);
            Map<String, Node> auraMap = getAllNodes(NodeType.AURA);

            //add gateway to oms and aura links
            for (String source : gatewayMap.keySet()) {
                Map<String, State> sourceMap = new HashMap<>();
                links.put(source, sourceMap);
                for (String target : omsMap.keySet()) {
                    sourceMap.put(target, State.CLOSED);
                }
                for (String target : auraMap.keySet()) {
                    sourceMap.put(target, State.CLOSED);
                }
            }
            //add oms to dfix links
            for (String source : omsMap.keySet()) {
                Map<String, State> sourceMap = new HashMap<>();
                links.put(source, sourceMap);
                for (String target : dfixMap.keySet()) {
                    sourceMap.put(target, State.CLOSED);
                }
            }
        } else {
            Map<String, Node> tempNodeMap = getAllNodes();
            for (Map.Entry<String, Map<String, State>> sourceEntry : links.entrySet()) {
                for (Map.Entry<String, State> targetEntry : sourceEntry.getValue().entrySet()) {
                    if (targetEntry.getValue() == State.CONNECTED) {
                        if (tempNodeMap.get(sourceEntry.getKey()).getState() != State.CONNECTED ||
                                tempNodeMap.get(targetEntry.getKey()).getState() != State.CONNECTED) {
                            targetEntry.setValue(State.CLOSED);
                            logger.info("Changing link status of dead nodes: {} -> {}",
                                    sourceEntry.getKey(), targetEntry.getKey());
                        }
                    } else if (targetEntry.getValue() == State.CONNECTING) {
                        logger.info("Link status is in transient CONNECTING state: {} -> {}",
                                sourceEntry.getKey(), targetEntry.getKey());
                        if (tempNodeMap.get(sourceEntry.getKey()).getState() == State.CONNECTED ||
                                tempNodeMap.get(targetEntry.getKey()).getState() == State.CONNECTED) {
                            targetEntry.setValue(State.CONNECTED);
                            logger.info("Link status changed from CONNECTING to CONNECTED: {} -> {}",
                                    sourceEntry.getKey(), targetEntry.getKey());
                        }
                    }

                    //todo remove this once the connecting logic is fixed from gateway
                    if (sourceEntry.getKey().startsWith("AURA")
                            && getNode(sourceEntry.getKey()).getState() == State.CONNECTED
                            && getNode(targetEntry.getKey()).getState() == State.CONNECTED) {
                        targetEntry.setValue(State.CONNECTED);
                    }
                }

            }
        }
    }

    public void rePopulateLinks() {
        Map<String, Map<String, State>> tempLinks = new HashMap<>();
        if (links == null) {
            links = new HashMap<>();
            populateLinks();
        }
        if (!links.isEmpty()) {
            Map<String, Node> gatewayMap = getAllNodes(NodeType.GATEWAY);
            Map<String, Node> omsMap = getAllNodes(NodeType.OMS);
            Map<String, Node> dfixMap = getAllNodes(NodeType.DFIX);
            Map<String, Node> auraMap = getAllNodes(NodeType.AURA);

            for (String source : gatewayMap.keySet()) {
                Map<String, State> sourceMap = new HashMap<>();
                tempLinks.put(source, sourceMap);
                for (String target : omsMap.keySet()) {
                    if (links.containsKey(source) && links.get(source).containsKey(target)) {
                        sourceMap.put(target, links.get(source).get(target));
                    } else {
                        sourceMap.put(target, State.CLOSED);
                    }
                }
                for (String target : auraMap.keySet()) {
                    if (links.containsKey(source) && links.get(source).containsKey(target)) {
                        sourceMap.put(target, links.get(source).get(target));
                    } else {
                        sourceMap.put(target, State.CLOSED);
                    }

                    //todo remove this once the connecting logic is fixed from gateway
                    if (getNode(source).getState() == State.CONNECTED
                            && getNode(target).getState() == State.CONNECTED) {
                        sourceMap.put(target, State.CONNECTED);
                    }
                }
            }
            for (String source : omsMap.keySet()) {
                Map<String, State> sourceMap = new HashMap<>();
                tempLinks.put(source, sourceMap);
                for (String target : dfixMap.keySet()) {
                    if (links.containsKey(source) && links.get(source).containsKey(target)) {
                        State linkState = links.get(source).get(target);
                        if (linkState == State.CONNECTING &&
                                omsMap.get(source).getState() == State.CONNECTED &&
                                dfixMap.get(target).getState() == State.CONNECTED) {
                            sourceMap.put(target, State.CONNECTED);
                            logger.info("Link status changed from CONNECTING to CONNECTED: {} -> {}", source, target);
                        } else {
                            sourceMap.put(target, links.get(source).get(target));
                        }
                    } else {
                        sourceMap.put(target, State.CLOSED);
                    }
                }
            }
            links = tempLinks;
        }
    }

    public Map<String, Map<String, State>> addToLinks(LinkStatus linkStatus) {
        if (links.isEmpty()) {
            populateLinks();
        }
        Map<String, State> sourceMap;
        if (links.containsKey(linkStatus.getSourceNode())) {
            sourceMap = links.get(linkStatus.getSourceNode());
        } else {
            sourceMap = new HashMap<>();
            links.put(linkStatus.getSourceNode(), sourceMap);
        }
        if (linkStatus.getState() == State.CONNECTED) {
            if (getNode(linkStatus.getSourceNode()).getState() != State.CLOSED &&
                    getNode(linkStatus.getDestinationNode()).getState() != State.CLOSED) {
                sourceMap.put(linkStatus.getDestinationNode(), linkStatus.getState());
            } else {
                sourceMap.put(linkStatus.getDestinationNode(), State.CONNECTING);
            }
        } else {
            sourceMap.put(linkStatus.getDestinationNode(), linkStatus.getState());
        }
        return links;
    }

    public Map<String, Map<String, State>> getLinks() {
        return links;
    }

    public void copyLinks(View from) {
        links = from.getLinks();
    }

    public short getPrimary(NodeType nodeType) {
        short primary = Short.MAX_VALUE;
        Map<String, Node> allNodesOfType = getAllNodes(nodeType);
        for (Node node : allNodesOfType.values()) {
            if (node.getState() == State.CONNECTED && primary > node.getId()) {
                primary = node.getId();
            }
        }
        if (primary == Short.MAX_VALUE) {
            primary = 0;
        }
        return primary;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (defaultNodes != null) {
            StringJoiner s = new StringJoiner(", ", "(", ")");
            for (Integer i : defaultNodes.values()) {
                s.add(String.valueOf(i));
            }
            sb.append(s.toString());
        } else {
            sb.append("(0, 0, 0, 0, 0)");
        }
        for (Node node : getAllNodes().values()) {
            sb.append("\n");
            sb.append(node);
        }
        return sb.toString();
    }

    //need to remove this json construction using a library call
    public String toJsonMetric() {
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"view\"");
        builder.append(",\"nodes\" : [");
        int i = 0;
        for (Node node : getAllNodes().values()) {
            if (++i != 1) builder.append(",");
            builder.append("{");
            builder.append("\"nodeName\" :" + "\"" + node.getName() + "\"");
            builder.append(",\"state\" :" + "\"" + node.getState() + "\"");
            builder.append(",\"ip\" :" + "\"" + node.getIpAddress() + "\"");
            if (node.getMetrics() != null) {
                JvmMetrics jvmMetrics = node.getMetrics().getJvmMetrics();
                builder.append(",\"metric\" :");
                builder.append("{");
                builder.append("\"node\" :" + "\"" + node.getName() + "\"")
                        .append(",\"maxMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getMaxMemory()) + "\"")
                        .append(",\"usedMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getUsedMemory()) + "\"")
                        .append(",\"freeMemory\" :" + "\"" + Formatters.formatWithoutFractions(jvmMetrics.getFreeMemory()) + "\"")
                        .append(",\"threadCount\" :" + jvmMetrics.getThreadCount())
                        .append(",\"processCpuUsage\" :" + "\"" + Formatters.format(jvmMetrics.getProcessCpuUsage()) + "\"")
                        .append(",\"systemCpuUsage\" :" + "\"" + Formatters.format(jvmMetrics.getSystemCpuUsage()) + "\"")
                        .append(",\"lastUpdateTime\" :" + "\"" + Formatters.format(jvmMetrics.getLastUpdateTime()) + "\"");
                builder.append("}");
            }
            builder.append("}");
        }
        builder.append("]");

        return builder.append("}").toString();
    }
}
