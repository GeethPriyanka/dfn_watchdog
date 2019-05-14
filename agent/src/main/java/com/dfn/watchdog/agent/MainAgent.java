package com.dfn.watchdog.agent;

import com.dfn.watchdog.agent.listeners.AgentCallbackListener;
import com.dfn.watchdog.agent.listeners.AgentCallbackListenerSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

/**
 * Test class for the Watchdog Agent library.
 * <p>
 * Initialize the agent.
 * Reads system in for client ids and print out the next node to route.
 */
public class MainAgent {
    private static final Logger logger = LoggerFactory.getLogger(MainAgent.class);

    public static void main(String[] args) {
        AgentCallbackListener listener = new AgentCallbackListenerSimple();
        if (args.length > 2) {
            WatchdogAgent.INSTANCE.configure(listener, Executors.newCachedThreadPool(), args[3])
                    .setNode(Short.valueOf(args[0]), args[1])
                    .enableWatchdog(true)
                    .build()
                    .run();
        } else if (args.length > 0) {
            WatchdogAgent.INSTANCE.configure(listener, Executors.newCachedThreadPool(), args[0]).run();
        } else {
            WatchdogAgent.INSTANCE.configure(listener, Executors.newCachedThreadPool())
                    .build()
                    .run();
        }

        readCustomerFromConsoleAndPrintNextNode(listener);
    }

    private static void readCustomerFromConsoleAndPrintNextNode(AgentCallbackListener listener) {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String msg = console.readLine();
                if (msg == null || "bye".equalsIgnoreCase(msg)) {
                    break;
                } else {
                    logger.info("*******************************************************************");
                    long start = System.nanoTime();
                    short next = listener.next(Long.parseLong(msg));
                    long end = System.nanoTime();
                    if (next > 0)
                        logger.info("Route to: OMS-{}, Time taken to generate: {}ns", next, (end - start));
                    else
                        logger.info("keep the messages in the queue");
                    logger.info("*******************************************************************");
                }

            } catch (Exception e) {
                logger.error("Invalid input: ", e);
            }
        }
    }
}