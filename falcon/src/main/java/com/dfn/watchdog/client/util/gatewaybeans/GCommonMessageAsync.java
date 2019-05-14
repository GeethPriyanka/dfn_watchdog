package com.dfn.watchdog.client.util.gatewaybeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * Created by dasunp on Nov, 2018
 */
public class GCommonMessageAsync {
    @JsonProperty(value = "HED")
    private GHeader header;
    @JsonProperty(value = "DAT")
    private GCommonMessageAsync.BodyWithCustomerId data;
    @JsonProperty(value = "TIMESTAMP")
    private Timestamp timestamp;

    public GHeader getHeader() {
        return header;
    }

    public void setHeader(GHeader header) {
        this.header = header;
    }

    public GCommonMessageAsync.BodyWithCustomerId getData() {
        return data;
    }

    public void setData(GCommonMessageAsync.BodyWithCustomerId data) {
        this.data = data;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class BodyWithCustomerId {
        @JsonProperty(value = "cusId")
        long customerId;

        public long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(long cusId) {
            this.customerId = cusId;
        }
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "{" +
                "\"header\":" + header +
                ", \"data\":" + data +
                '}';
    }
}
