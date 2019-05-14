package com.dfn.watchdog.client.database.queues;

import com.dfn.watchdog.client.database.DatabaseUtil;
import com.dfn.watchdog.client.database.pojo.SessionPojo;
import com.dfn.watchdog.client.util.ClientProperties;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by dasunp on Oct, 2018
 */
@Configuration
@EnableScheduling
public class SessionQueue {
    private Queue<com.dfn.watchdog.client.database.pojo.SessionPojo> sessionQueue = new LinkedList<>();
    private static final Logger logger = LogManager.getLogger(SessionQueue.class);
    private ClientProperties properties;

    private int delay;

    public SessionQueue() {
        // Default Constructor
    }

    public SessionQueue(ClientProperties properties) {
        this.properties = properties;
        this.delay = properties.getQueueCounts().getSessionQueueTimeout();
    }

    public void enqueue(com.dfn.watchdog.client.database.pojo.SessionPojo session) {
        logger.info("Session " + session.getSessionId() + " enqueued.");
        sessionQueue.add(session);
    }

    private com.dfn.watchdog.client.database.pojo.SessionPojo dequeue() {
        return this.sessionQueue.poll();
    }

    public int getSize() {
        return sessionQueue.size();
    }

    public void emit(ClientProperties properties) {
        try {
            if(!sessionQueue.isEmpty()) DatabaseUtil.getInstance(properties).insertSessionsBatch(sessionQueue);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        logger.warn("Emit completed. Size: " + sessionQueue.size());
    }

    public Queue<SessionPojo> getSessionQueue() {
        return sessionQueue;
    }

    /**
     * Schedule the db batch service
     * find out a way to get the config value to here.
     */
    /*@Scheduled(fixedDelay = 10000)
    public void emitWithScheduler()  {
        emit(properties);
    }
    */
}
