package com.dfn.watchdog.client.database.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by dasunp on Aug, 2018
 * This is used to deserialize the SQL Resultset.
 * Used with Apache Commons DBUtils
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SessionPojo {

    private String sessionId;
    private long loginId;
    private Timestamp expiryTime;
    private Timestamp startTime;
    private String clientIp;
    private int clientChannel;
    private Timestamp logoutTime;
    private String upTime;
    private String status;
    private Date date;

    public SessionPojo() {
        // Default Constructor
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getLoginId() {
        return loginId;
    }

    public Timestamp getExpiryTime() {
        return expiryTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public int getClientChannel() {
        return clientChannel;
    }

    public void setLoginId(long loginId) {
        this.loginId = loginId;
    }

    public void setExpiryTime(Timestamp expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setClientChannel(int clientChannel) {
        this.clientChannel = clientChannel;
    }

    public Timestamp getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Timestamp logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getUpTime() {
        return upTime;
    }

    public void setUpTime(String upTime) {
        this.upTime = upTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SessionPojo{" +
                "sessionId='" + sessionId + '\'' +
                ", loginId=" + loginId +
                ", expiryTime=" + expiryTime +
                ", startTime=" + startTime +
                ", clientIp='" + clientIp + '\'' +
                ", clientChannel=" + clientChannel +
                ", logoutTime=" + logoutTime +
                ", upTime='" + upTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
