package com.dfn.watchdog.commons.messages.monitoring;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.View;

import java.text.DecimalFormat;

/**
 * Holder for system metrics.
 */
public class SystemMetrics implements MonitoringMessage {
    private long failedMessages;
    private long disconnects;
    private long tps;
    private double rtt;
    private String network;
    private long clients;
    private long requests;

    public SystemMetrics() {
        failedMessages = 0;
        disconnects = 0;
        tps = 0;
        rtt = 10;
        network = "OK";
        clients = 0;
        requests = 0;
    }

    public SystemMetrics calculate(View view) {
        failedMessages = 0;
        disconnects = 0;
        for (Node node : view.getAllNodes().values()) {
            NodeMetrics metric =  node.getMetrics();
            if (metric != null) {
                failedMessages += metric.getFailedMessages();
                disconnects += metric.getDisconnects();
            }
        }
        return this;
    }

    public void setTps(long tps) {
        if (tps != 0) {
            this.tps = tps;
        }
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public void setClients(long clients) {
        this.clients = clients;
    }

    public String toJson() {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
        StringBuilder builder = new StringBuilder("{\"messageType\" : \"sys_metric\"");
        builder.append(",\"failed\" :").append(failedMessages)
                .append(",\"disconnects\" :").append(disconnects)
                .append(",\"tps\" :").append(tps)
                .append(",\"requests\" :").append(requests)
                .append(",\"rtt\" :" + "\"").append(decimalFormat.format(rtt)).append("\"")
                .append(",\"network\" :").append("\"").append(network).append("\"")
                .append(",\"clients\" :").append(clients);

        return builder.append("}").toString();
    }
}