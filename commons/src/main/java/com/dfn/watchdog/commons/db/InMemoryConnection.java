package com.dfn.watchdog.commons.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface for database connection.
 */
public class InMemoryConnection implements DatabaseConnection {
    private final Logger logger = LoggerFactory.getLogger(InMemoryConnection.class);
    private static final String CLIENT_ID = "client_id";
    private static final String NEXT_NODE = "next_node";
    private static final String UPDATE_TIME = "update_time";

    private Map<Long, Map<String, String>> storage;
    private final SimpleDateFormat dateFormatter;
    private DatabaseConfig dbConfig;

    public InMemoryConnection(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }

    public void connect() {
        storage = new ConcurrentHashMap<>();
        logger.warn("Client routes saved in {} will be lost on restart", dbConfig.getName());
    }

    public void disconnect() {
        storage.clear();
    }

    public String getClientFromDb(long clientId) {
        Map<String, String> clientEntry = storage.get(clientId);
        return clientEntry.get(NEXT_NODE);
    }

    public void setClientInDb(long clientId, short next) {
        Map<String, String> clientEntry = new HashMap<>(3);
        clientEntry.put(CLIENT_ID, String.valueOf(clientId));
        clientEntry.put(NEXT_NODE, String.valueOf(clientId));
        clientEntry.put(UPDATE_TIME, dateFormatter.format(new Date()));
        logger.info("Setting client route in memory: {}", clientEntry);

        storage.put(clientId, clientEntry);
    }

    public void updateNodeFailure(short failedNode, short backupNode) {
        int changedEntries = 0;
        for (Map.Entry<Long, Map<String, String>> clientEntry : storage.entrySet()) {
            short nodeId = Short.parseShort(clientEntry.getValue().get(NEXT_NODE));
            if (nodeId == failedNode) {
                clientEntry.getValue().put(NEXT_NODE, String.valueOf(backupNode));
                ++changedEntries;
            }
        }
        logger.info("Updated {} entries in memory with the backup client route", changedEntries);
    }

    public List<Map<String, String>> getAllRoutes() {
        List<Map<String, String>> clientRoutes = new ArrayList<>();
        for (Map<String, String> routeMap : storage.values()) {
            Map<String, String> route = new HashMap<>(2);
            route.put("client", routeMap.get(CLIENT_ID));
            route.put("nextNode", routeMap.get(NEXT_NODE));
            clientRoutes.add(route);
        }
        return clientRoutes;
    }

    public List<Map<String, String>> getRouteHistory(long customerId) {
        List<Map<String, String>> clientRouteHistory = new ArrayList<>();
        logger.warn("In memory route storage does not support history. Request for {} ignored", customerId);
        return clientRouteHistory;
    }

    @Override
    public void checkConnectivity() throws Exception {

    }
}
