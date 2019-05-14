package com.dfn.watchdog.client.database.pojo;

import java.sql.Timestamp;

/**
 * Created by dasunp on Aug, 2018
 * This POJO can be treated as a ResponsePOJO as well.
 */
public final class MessagePojo {
    private String unique_request_id;
    private int message_type;
    private int channel;
    private String comm_ver;
    private int login_id;
    private String session_id;
    private String client_ip;
    private String tenantCode;
    private String customer_id;
    private String message;
    private Timestamp date;
    private long responseTime;

    public String getUnique_request_id() {
        return unique_request_id;
    }

    public void setUnique_request_id(String unique_request_id) {
        this.unique_request_id = unique_request_id;
    }

    public int getMessage_type() {
        return message_type;
    }

    public void setMessage_type(int message_type) {
        this.message_type = message_type;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getComm_ver() {
        return comm_ver;
    }

    public void setComm_ver(String comm_ver) {
        this.comm_ver = comm_ver;
    }

    public int getLogin_id() {
        return login_id;
    }

    public void setLogin_id(int login_id) {
        this.login_id = login_id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getClient_ip() {
        return client_ip;
    }

    public void setClient_ip(String client_ip) {
        this.client_ip = client_ip;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
