package com.dfn.watchdog.client.util;

/**
 * Created by dasunp on Aug, 2018
 * Used as a container object to contain the data from the proxied response of DFNGateway.
 */
public class SessionPojo {

    private long loginId;
    private String sessionToken;
    private String expiryTime;
    private String startTime;
    private String clientIp;
    private String clientChannel;

    public SessionPojo() { }

    public long getLoginId() {
        return loginId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getClientChannel() {
        return clientChannel;
    }

    public void setLoginId(long loginId) {
        this.loginId = loginId;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setClientChannel(String clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public String toString() {
        return "SessionPojo{" +
                "loginId=" + loginId +
                ", sessionToken='" + sessionToken + '\'' +
                ", expiryTime='" + expiryTime + '\'' +
                ", startTime='" + startTime + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", clientChannel='" + clientChannel + '\'' +
                '}';
    }
}
