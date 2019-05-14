package com.dfn.watchdog.client.database;

/**
 * Created by dasunp on Aug, 2018
 * Lists all the SQL queries used in EmbeddedDatabaseUtility class.
 * Queries are utilized with Prepared Statements to prevent SQL injections.
 * Table creation queries and tuple retrieval queries are specified here.
 */
final class StandaloneDatabaseQueries {

    private static final String VALUES = "VALUES";
    
    private StandaloneDatabaseQueries() {
        // Default Constructor
    }

    /**
     * Create sessions relation
     */
    static final String SESSION_TABLE_CREATION_QUERY = "CREATE TABLE IF NOT EXISTS sessions(" +
            "sessionId VARCHAR(255) NOT NULL," +
            "loginId LONG, " +
            "expiryTime TIMESTAMP, " +
            "startTime TIMESTAMP," +
            "clientIp VARCHAR(255)," +
            "clientChannel INTEGER," +
            "logoutTime TIMESTAMP," +
            "status VARCHAR(100)," +
            "date DATE," +
            "PRIMARY KEY (sessionId)" +
            ")";

    /**
     * Create messages relation
     */
    static final String CREATE_MESSAGES_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS messages(" +
            "id INT NOT NULL AUTO_INCREMENT," +
            "unique_request_id VARCHAR," +
            "message_type INT," +
            "channel INT," +
            "comm_ver VARCHAR(255)," +
            "login_id INT," +
            "session_id VARCHAR(255)," +
            "client_ip VARCHAR(255)," +
            "tenantCode VARCHAR(255)," +
            "customer_id INT," +
            "message TEXT," +
            "date TIMESTAMP," +
            "responseTime LONG," +
            "persistingTimestamp TIMESTAMP," +
            "falconTime TIMESTAMP," +
            "PRIMARY KEY (id)" +
            ")";

    static final String CREATE_RESPONSES_QUERY = "CREATE TABLE IF NOT EXISTS responses(" +
            "id INT NOT NULL AUTO_INCREMENT," +
            "unique_request_id VARCHAR(255)," +
            "message_type INT," +
            "channel INT," +
            "comm_ver VARCHAR(255)," +
            "login_id INT," +
            "session_id VARCHAR(255)," +
            "client_ip VARCHAR(255)," +
            "tenantCode VARCHAR(255)," +
            "message TEXT," +
            "date TIMESTAMP," +
            "persistingTimestamp TIMESTAMP," +
            "falconTime TIMESTAMP," +
            "PRIMARY KEY(id)" +
            ")";

    /**
     * Retrieve one particular session.
     */
    static final String SESSION_RETRIEVAL_QUERY = "SELECT * FROM sessions WHERE loginId=?";

    /**
     * Retrieve all the sessions.
     */
    static final String SESSION_ALL_RETRIEVAL_QUERY = "SELECT * FROM sessions ORDER BY startTime DESC";

    /**
     * Retrieve persisted messages
     */
    static final String RETRIEVE_MESSAGES_QUERY = "SELECT * FROM messages ORDER BY date DESC";

    static final String RETRIEVE_SPECIFIC_MESSAGE = "SELECT * FROM messages WHERE session_id=? ORDER BY date DESC";

    static final String RETRIEVE_RESPONSES_QUERY = "SELECT * FROM responses ORDER BY date DESC";

    static final String RETRIEVE_SPECIFIC_RESPONSES_QUERY = "SELECT * FROM responses WHERE unique_request_id=? ORDER BY date DESC";

    static final String ACTIVE_SESSIONS_QUERY = "SELECT * FROM sessions WHERE status='ACTIVE' ORDER BY startTime DESC";

    /**
     * Insert a session into the database.
     */
    static final String SESSION_INSERTION_QUERY = "INSERT INTO sessions " +
            "(" +
            "sessionId," +
            "loginId," +
            "expiryTime," +
            "startTime," +
            "clientIp," +
            "clientChannel," +
            "logoutTime," +
            "status," +
            "date" +
            ")" +
            VALUES +
            "(" +
            "?,?,?,?,?,?,?,?,CURRENT_DATE()" +
            ")";

    static final String UPDATE_EXPIRED_SESSION = "UPDATE sessions SET status=? WHERE loginId=?";

    /**
     * Insert messages
     */
    static final String INSERT_MESSAGES_QUERY = "INSERT INTO messages(" +
            "unique_request_id," +
            "message_type," +
            "channel," +
            "comm_ver," +
            "login_id," +
            "session_id," +
            "client_ip," +
            "tenantCode," +
            "customer_id," +
            "message," +
            "date," +
            "responseTime," +
            "falconTime," +
            "persistingTimestamp," +
            ")" +
            VALUES +
            "(" +
            "?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP()" +
            ")";

    /**
     * Retrieve persisted messages
     */

    static final String INSERT_RESPONSES_QUERY = "INSERT INTO responses(" +
            "unique_request_id," +
            "message_type," +
            "channel," +
            "comm_ver," +
            "login_id," +
            "session_id," +
            "client_ip," +
            "tenantCode," +
            "message," +
            "date," +
            "falconTime," +
            "persistingTimestamp" +
            ")" +
            VALUES +
            "(" +
            "?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP()" +
            ")";

    static final String DISTINCT_MESSAGE_TYPES = "SELECT DISTINCT message_type FROM messages WHERE session_id=?";

    static final String COUNT_DISTINCT_MESSAGES = "SELECT COUNT(message) FROM messages GROUP BY (session_id, message_type) HAVING session_id=? AND message_type=?";

    static final String RETRIEVE_SPECIFIC_TIMESTAMP = "SELECT date FROM messages WHERE unique_request_id=?";

    static final String UPDATE_RESPONSE_TIME = "UPDATE messages SET responseTime=? WHERE unique_request_id=?";

    static final String RETRIEVE_SLA_MESSAGES_QUERY = "SELECT * FROM messages WHERE message_type != 1 ORDER BY date DESC";

    static final String RETRIEVE_CLIENT_COUNTS = "SELECT COUNT(sessionId) FROM sessions WHERE startTime = ?";

    static final String RETRIEVE_START_TIMES = "SELECT startTime from sessions";

    static final String STARTTIME_COUNT_QUERY = "SELECT date, COUNT(*) as `value` FROM SESSIONS GROUP BY date";

}
