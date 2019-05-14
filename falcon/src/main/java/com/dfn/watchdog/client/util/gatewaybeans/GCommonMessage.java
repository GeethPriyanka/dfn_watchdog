package com.dfn.watchdog.client.util.gatewaybeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gateway message with customerId for data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GCommonMessage {
    @JsonProperty(value = "HED")
    private GHeader header;
    @JsonProperty(value = "DAT")
    private BodyWithCustomerId data;

    public GHeader getHeader() {
        return header;
    }

    public void setHeader(GHeader header) {
        this.header = header;
    }

    public BodyWithCustomerId getData() {
        return data;
    }

    public void setData(BodyWithCustomerId data) {
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
}
