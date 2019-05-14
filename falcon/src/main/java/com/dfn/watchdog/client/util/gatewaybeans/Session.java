package com.dfn.watchdog.client.util.gatewaybeans;

import akka.actor.ActorRef;
import io.netty.channel.Channel;

/**
 * Created by dasunp on Oct, 2018
 */
public class Session {

    enum Status {
        AUTHENTICATED, WAITING_OTP, EXPIRED, CHANGE_PASSWORD_REQUIRED, INVALID, INIT
    }

    private long loginId;
    private String sessionToken;
    private ActorRef clientActor;
    private GHeader header;
    private Status status;
    private long expiryTime;
    private Channel customerChannel;
    private long startTime;
    private String sessionStatus;

    public long getLoginId() {
        return loginId;
    }

    public void setLoginId(long loginId) {
        this.loginId = loginId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public ActorRef getClientActor() {
        return clientActor;
    }

    public void setClientActor(ActorRef clientActor) {
        this.clientActor = clientActor;
    }

    public GHeader getHeader() {
        return header;
    }

    public void setHeader(GHeader header) {
        this.header = header;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Channel getCustomerChannel() {
        return customerChannel;
    }

    public void setCustomerChannel(Channel customerChannel) {
        this.customerChannel = customerChannel;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    /**
     * @return
     */
    public String getClientIp() {
        return header.getClientIp();
    }
}
