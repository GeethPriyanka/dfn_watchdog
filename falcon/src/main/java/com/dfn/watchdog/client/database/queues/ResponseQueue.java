package com.dfn.watchdog.client.database.queues;

import com.dfn.watchdog.client.database.DatabaseUtil;
import com.dfn.watchdog.client.util.ClientProperties;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonResponseAsync;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonResponseDb;
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
public class ResponseQueue {

    private Queue<GCommonResponseAsync> responseQueue = new LinkedList<>();
    private static final Logger logger = LogManager.getLogger(ResponseQueue.class);
    private ClientProperties properties;

    private int delay;

    public ResponseQueue() {
        // Default Constructor
    }

    public ResponseQueue(ClientProperties properties) {
        this.properties = properties;
        this.delay = properties.getQueueCounts().getResponseQueueTimeout();
    }

    public void enqueue(GCommonResponseAsync gCommonResponseDb) {
        logger.info("Reponse for " + gCommonResponseDb.getHeader().getRequestId() + " enqueued.");
        this.responseQueue.add(gCommonResponseDb);
    }

    public GCommonResponseAsync dequeue() {
        return this.responseQueue.poll();
    }

    public int getSize() {
        return responseQueue.size();
    }

    public void emit(ClientProperties properties) {
        try {
            if(!responseQueue.isEmpty()) DatabaseUtil.getInstance(properties).insertResponseBatch(responseQueue);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        logger.warn("Emit completed. Size: " + responseQueue.size());
    }

    /**
     * schedule the batch db service
     */
    /*@Scheduled(fixedDelay = 10000)
    public void emitWithScheduler() {
        emit(properties);
    }
    */
}

