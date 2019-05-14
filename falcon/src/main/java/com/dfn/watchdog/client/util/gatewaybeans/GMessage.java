package com.dfn.watchdog.client.util.gatewaybeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gateway top level message.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GMessage<T extends GData> {
    @JsonProperty(value = "HED")
    private GHeader header;
    @JsonProperty(value = "DAT")
    private T data;

    public GMessage() {
     //default constructor
    }

    public GMessage(GHeader header) {
        this.header = header;
    }

    public GMessage(GHeader header, T data) {
        this.header = header;
        this.data = data;
    }

    public GHeader getHeader() {
        return header;
    }

    public void setHeader(GHeader header) {
        this.header = header;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
