package com.dfn.watchdog.commons.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.isi.security.GNUCrypt;

import java.security.InvalidKeyException;

/**
 * Server database configurations.
 */
public class DatabaseConfig {
    @JsonProperty
    private String name;
    @JsonProperty
    private String ip;
    @JsonProperty
    private int port;
    @JsonProperty
    private String url;
    @JsonProperty
    private String user;
    @JsonProperty
    private String password;
    @JsonProperty
    private String cluster;

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        try {
            return GNUCrypt.decrypt("@1B2c3D4e5F6g7H8", password);
        } catch (InvalidKeyException e) {
            return null;
        }
    }

    public String getCluster() {
        return cluster;
    }

    public String prettyPrint() {
        return "\n{ \n" +
                "\t" + "NAME: " + name + "\n" +
                "\t" + "IP: " + ip + "\n" +
                "\t" + "PORT: " + port + "\n" +
                "\t" + "URL: " + url + "\n" +
                "\t" + "CLUSTER: " + cluster + "\n" +
                "}\n";
    }
}
