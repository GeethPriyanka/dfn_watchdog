package com.dfn.watchdog.client.util.gatewaybeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Gateway top level message Header section.
 * TODO: Remove @JsonIgnoreProperties line.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GHeader implements Serializable {
    @JsonProperty(value = "msgTyp")
    private int serviceId;
    @JsonProperty(value = "tenantCode")
    private String tenantCode;
    @JsonProperty(value = "sesnId")
    private String sessionId;
    @JsonProperty(value = "loginId")
    private long loginId;
    @JsonProperty(value = "channel")
    private int channel;
    @JsonProperty(value = "clientIp")
    private String clientIp;
    @JsonProperty(value = "commVer")
    private String commVer;
    @JsonProperty(value = "unqReqId")
    private String requestId;
    @JsonProperty(value = "routeId")
    private long routeId;


    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
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

    public void setLoginId(long loginId) {
        this.loginId = loginId;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getCommVer() {
        return commVer;
    }

    public void setCommVer(String commVer) {
        this.commVer = commVer;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }
}
