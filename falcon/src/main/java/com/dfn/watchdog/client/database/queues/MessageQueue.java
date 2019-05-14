package com.dfn.watchdog.client.database.queues;

/**
 * Created by dasunp on Oct, 2018
 */

import com.dfn.watchdog.client.database.DatabaseUtil;
import com.dfn.watchdog.client.util.ClientProperties;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonMessageAsync;
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
 * A generic can be used here for further developments.
 */
@Configuration
@EnableScheduling
public class MessageQueue {

    private Queue<GCommonMessageAsync> messageQueue = new LinkedList<>();
    private static final Logger logger = LogManager.getLogger(MessageQueue.class);
    private ClientProperties properties;

    private int delay;

    public MessageQueue() {
        // Default Constructor
    }

    public MessageQueue(ClientProperties properties) {
        this.properties = properties;
        this.delay = properties.getQueueCounts().getMessageQueueTimeout();
    }

    public void enqueue(GCommonMessageAsync message) {
        logger.info("Message " + message.getHeader().getRequestId() + " enqueued.");
        messageQueue.add(message);
    }

    public GCommonMessageAsync dequeue() {
        return this.messageQueue.poll();
    }

    public int getSize() {
        return messageQueue.size();
    }

    public void emit(ClientProperties properties) {
        try {
            if(!messageQueue.isEmpty()) DatabaseUtil.getInstance(properties).insertMessageBatch(messageQueue);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        logger.warn("Emit completed. Size: " + messageQueue.size());
    }

    /**
     * Schedule the db batch service
     */
    /*@Scheduled(fixedDelay = 1000)
    public void emitWithScheduler() {
        emit(properties);
    }
    */
}

