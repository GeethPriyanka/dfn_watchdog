package com.dfn.watchdog.client;

/**
 * Connected client session.
 */
public class Service {
    private int id;
    private String serviceName;
    private String location;
    private String type;
    private String url;

    public Service() {
    }

    public Service(int id, String serviceName, String location, String type, String url) {
        this.id = id;
        this.serviceName = serviceName;
        this.location = location;
        this.type = type;
        this.url = url;
    }


    public int getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }
}
