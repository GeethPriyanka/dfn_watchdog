package com.dfn.watchdog.commons;

import com.dfn.watchdog.commons.messages.Streamable;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.channel.Channel;

/**
 * System representation of a component.
 */
public class Node implements Streamable, Comparable<Node> {
    private short id;
    private NodeType type;
    private String ipAddress;
    private State state;
    private BackupState backupState;
    private String tradeIpAddress;
    private int tradePort;

    private transient NodeMetrics metrics;
    private transient long lastHeartbeat;
    private transient Channel channel;
    private transient Channel secondaryChannel;

    public Node(short id, NodeType type, State state) {
        this.id = id;
        this.type = type;
        this.state = state;
        backupState = BackupState.CLOSED;
        ipAddress = "";
        tradeIpAddress = "";

        metrics = new NodeMetrics(id, type);
        channel = new EmptyChannel();
    }

    public Node setId(short id) {
        this.id = id;
        return this;
    }

    public Node setType(NodeType type) {
        this.type = type;
        return this;
    }

    public Node setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public Node changeState(State state) {
        switch (state) {
            case CONNECTING:
                this.state = state;
                break;
            case CLOSED:
                handleStateChangeClosed();
                break;
            default:
                this.state = state;
        }
        return this;
    }

    public Node changeBackupState(BackupState state) {
        switch (state) {
            case CLOSED:
                this.backupState = state;
                break;
            case RECOVERING:
                this.backupState = state;
                break;
            case RECOVERED:
                this.backupState = state;
                break;
            default:
                this.backupState = state;
        }
        return this;
    }

    public Node setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public Node updateLastHeartbeat() {
        lastHeartbeat = System.currentTimeMillis();
        return this;
    }

    public short getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getName() {
        return type + "-" + id;
    }

    public State getState() {
        return state;
    }

    public BackupState getBackupState() {
        return backupState;
    }

    public NodeMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(NodeMetrics metrics) {
        this.metrics = metrics;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    private void handleStateChangeClosed() {
        channel.disconnect();
        this.channel = new EmptyChannel();
        getMetrics().setPrevState(State.CLOSED);
        getMetrics().setLastUpdateTime(System.currentTimeMillis());
        if (state == State.SUSPENDED) {
            state = State.CLOSED;
            backupState = BackupState.RECOVERING;
        } else {
            state = State.CLOSED;
        }
    }

    Node copyValues(Node node) {
        this.state = node.getState();
        this.ipAddress = node.getIpAddress();
        this.backupState = node.getBackupState();
        this.lastHeartbeat = node.getLastHeartbeat();
        this.channel = node.getChannel();
        this.tradeIpAddress = node.getTradeIpAddress();
        this.tradePort = node.getTradePort();
        return this;
    }

    @Override
    public int compareTo(Node otherNode) {
        if (otherNode != null) {
            String nodeName = type.toString() + id;
            return (nodeName).compareTo(otherNode.getType().toString() + otherNode.getId());
        }
        return 1;
    }

    @Override
    public boolean equals(Object otherNode) {
        if (this == otherNode) {
            return  true;
        } else if (otherNode == null) {
            return false;
        } else if (otherNode instanceof Node && type.equals(((Node) otherNode).getType())) {
                return id == ((Node) otherNode).getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (type.toString() + id).hashCode();
    }

    @Override
    public String toString() {
        return "{Id: " + id + ", Type: " + type + ", State: " + state + ", IP: " + ipAddress + "}";
    }

    public int getTradePort() {
        return tradePort;
    }

    public Node setTradePort(int port) {
        this.tradePort = port;
        return this;
    }

    public String getTradeIpAddress() {
        return tradeIpAddress;
    }

    public Node setTradeIpAddress(String tradeIpAddress) {
        this.tradeIpAddress = tradeIpAddress;
        return this;
    }

    public Channel getSecondaryChannel() {
        return secondaryChannel;
    }

    public void setSecondaryChannel(Channel secondaryChannel) {
        this.secondaryChannel = secondaryChannel;
    }
}
