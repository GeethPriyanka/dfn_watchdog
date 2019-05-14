package com.dfn.watchdog.client.util.gatewaybeans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by dasunp on Sep, 2018
 * Created as a POJO for the purpose of database persistence.
 */
public class GCommonResponseDb implements GData{
    @JsonProperty(value = "HED")
    private GHeader header;
    @JsonProperty(value = "DAT")
    private Object data;

    public GCommonResponseDb(){
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

    public void setData(Object data) {
        this.data = data;
    }
}
