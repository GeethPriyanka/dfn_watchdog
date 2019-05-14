package com.dfn.watchdog.commons.stat;

import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.*;


/**
 * Holds the jmx beans and calculates system metrics.
 * Uses sun.management packages to gather statistics. Should be replaced if other jdk is to use.
 */
public class StatisticsJboss implements Statistics {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsJboss.class);

    private MemoryMXBean memoryBean;
    private RuntimeMXBean runtimeBean;

    private ThreadMXBean threadBean;
    private OperatingSystemMXBean osBean;

    public StatisticsJboss() {
        memoryBean = ManagementFactory.getMemoryMXBean();
        runtimeBean = ManagementFactory.getRuntimeMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        osBean = ManagementFactory.getOperatingSystemMXBean();

        logger.info("Loaded JBoss specific statistic module. Process information: " + runtimeBean.getName());
    }

    /**
     * @param metrics update the fields passed metric object
     * @return same NodeMetric passed
     */
    public JvmMetrics collect(JvmMetrics metrics) {
        metrics.setProcessCpuUsage(-1);
        metrics.setSystemCpuUsage(osBean.getSystemLoadAverage());
        metrics.setMaxMemory(memoryBean.getHeapMemoryUsage().getCommitted() / 1000000D);
        metrics.setUsedMemory(memoryBean.getHeapMemoryUsage().getUsed() / 1000000D);
        metrics.setFreeMemory(metrics.getMaxMemory() - metrics.getUsedMemory());
        metrics.setThreadCount(threadBean.getThreadCount());
        metrics.setLastUpdateTime(System.currentTimeMillis());
        return metrics;
    }
}
