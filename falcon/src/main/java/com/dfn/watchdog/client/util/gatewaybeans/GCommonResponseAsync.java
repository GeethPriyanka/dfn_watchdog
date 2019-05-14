package com.dfn.watchdog.client.util.gatewaybeans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * Created by dasunp on Sep, 2018
 * Created as a POJO for the purpose of database persistence.
 */
public class GCommonResponseAsync implements GData{
    @JsonProperty(value = "HED")
    private GHeader header;
    @JsonProperty(value = "DAT")
    private Object data;
    @JsonProperty(value = "TIMESTAMP")
    private Timestamp timestamp;

    public GCommonResponseAsync(){
        // Default Constructor
    }

    public GHeader getHeader() {
        return header;
    }

    public void setHeader(GHeader header) {
        this.header = header;
    }

    public Object getData() {
        return data;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
