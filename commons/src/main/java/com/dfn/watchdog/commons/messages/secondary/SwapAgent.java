package com.dfn.watchdog.commons.messages.secondary;

import com.dfn.watchdog.commons.messages.cluster.JoinMessage;

public class SwapAgent implements SecondaryMessage, JoinMessage {
    private boolean doSwapServer;
    private String serverIp;
    private int port;

    public SwapAgent(boolean doSwap) {
        this.doSwapServer = doSwap;
    }

    public boolean isDoSwapServer() {
        return doSwapServer;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getPort() {
        return port;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
