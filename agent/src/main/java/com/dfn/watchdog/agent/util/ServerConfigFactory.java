package com.dfn.watchdog.agent.util;

import com.dfn.watchdog.agent.WatchdogAgent;

public enum ServerConfigFactory {
    INSTANCE;

    private String primaryIp;
    private int primaryPort;
    private String secondaryIp;
    private int secondaryPort;
    private int swapCount = 0;

    public String getPrimaryIp() {
        if (primaryIp == null) {
            primaryIp = WatchdogAgent.INSTANCE.getProperties().serverIp();
        }
        return primaryIp;
    }

    public int getPrimaryPort() {
        if (primaryPort == 0) {
            primaryPort = WatchdogAgent.INSTANCE.getProperties().port();
        }
        return primaryPort;
    }

    public String getSecondaryIp() {
        if (secondaryIp == null) {
            secondaryIp = WatchdogAgent.INSTANCE.getProperties().secondaryServerIp();
        }
        return secondaryIp;
    }

    public int getSecondaryPort() {
        if (secondaryPort == 0) {
            secondaryPort = WatchdogAgent.INSTANCE.getProperties().secondaryServerPort();
        }
        return secondaryPort;
    }

    public boolean isSecondaryAvailable() {
        if (getSecondaryIp() == null || getSecondaryPort() == 0) {
            return false;
        }
        return true;
    }

    public void swapServers() {
        swapIps();
        swapPorts();
        ++swapCount;
    }

    private void swapIps() {
        String tempIp = getPrimaryIp();
        this.primaryIp = getSecondaryIp();
        this.secondaryIp = tempIp;
    }

    private void swapPorts() {
        int tempPort = getPrimaryPort();
        this.primaryPort = getSecondaryPort();
        this.secondaryPort = tempPort;
    }

    public int getSwapCount() {
        return swapCount;
    }
}
