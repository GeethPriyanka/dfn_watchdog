package com.dfn.watchdog.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * service configuration.
 */
public class ServiceConfiguration {
    @JsonProperty("locations")
    private List<ServiceLocation> serviceLocations;
    @JsonProperty("services")
    private List<Service> services;

    public List<ServiceLocation> getServiceLocations() {
        return serviceLocations;
    }

    public void setServiceLocations(List<ServiceLocation> serviceLocations) {
        this.serviceLocations = serviceLocations;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
