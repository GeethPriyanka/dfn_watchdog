package com.dfn.watchdog.commons.stat;

import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.ThreadMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;


/**
 * Holds the jmx beans and calculates system metrics.
 * Uses sun.management packages to gather statistics. Should be replaced if other jdk is to use.
 */
public class StatisticsGeneral implements Statistics {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsGeneral.class);

    private MemoryMXBean memoryBean;
    private RuntimeMXBean runtimeBean;

    private ThreadMXBean threadBean;
    private OperatingSystemMXBean osBean;

    public StatisticsGeneral() {
        memoryBean = ManagementFactory.getMemoryMXBean();
        runtimeBean = ManagementFactory.getRuntimeMXBean();
        threadBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        logger.info("Loaded general statistic module. Process information: " + runtimeBean.getName());
    }

    /**
     * @param metrics update the fields passed metric object
     * @return same NodeMetric passed
     */
    public JvmMetrics collect(JvmMetrics metrics) {
        metrics.setProcessCpuUsage(osBean.getProcessCpuLoad() * 100.0);
        metrics.setSystemCpuUsage(osBean.getSystemCpuLoad() * 100.0);
        metrics.setMaxMemory(memoryBean.getHeapMemoryUsage().getCommitted() / 1000000D);
        metrics.setUsedMemory(memoryBean.getHeapMemoryUsage().getUsed() / 1000000D);
        metrics.setFreeMemory(metrics.getMaxMemory() - metrics.getUsedMemory());
        metrics.setThreadCount(threadBean.getThreadCount());
        metrics.setLastUpdateTime(System.currentTimeMillis());
        return metrics;
    }
}
