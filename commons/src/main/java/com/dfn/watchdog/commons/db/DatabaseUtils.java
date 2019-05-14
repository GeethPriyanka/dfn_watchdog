package com.dfn.watchdog.commons.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static method for connecting to a database
 */
public class DatabaseUtils {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    private DatabaseUtils() {
        //utility class
    }

    public static DatabaseConnection connectToDatabase(DatabaseConfig databaseConfig) throws Exception {
        DatabaseConnection dbConnection;
        if (databaseConfig.getName().equalsIgnoreCase("CASSANDRA")) {
            dbConnection = new CassandraConnection(databaseConfig);
        } else if (databaseConfig.getName().equalsIgnoreCase("ORACLE")) {
            dbConnection = new OracleConnection(databaseConfig);
        } else if (databaseConfig.getName().equalsIgnoreCase("MEMORY")) {
            dbConnection = new InMemoryConnection(databaseConfig);
        } else {
            logger.warn("Invalid database name. Defaulting to in memory");
            dbConnection = new InMemoryConnection(databaseConfig);
        }
        dbConnection.connect();
        dbConnection.checkConnectivity();
        return dbConnection;
    }
}
