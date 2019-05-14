package com.dfn.watchdog.commons.stat;

import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.*;


/**
 * Holds the jmx beans and calculates system metrics.
 * Uses sun.management packages to gather statistics. Should be replaced if other jdk is to use.
 */
public class StatisticsCommon implements Statistics {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsCommon.class);

    private MemoryMXBean memoryBean;
    private RuntimeMXBean runtimeBean;
    private Runtime runtime;

    private ThreadMXBean threadBean;
    private OperatingSystemMXBean osBean;

    private File file;

    public StatisticsCommon() {
        memoryBean = ManagementFactory.getMemoryMXBean();
        runtimeBean = ManagementFactory.getRuntimeMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        osBean = ManagementFactory.getOperatingSystemMXBean();
        runtime = Runtime.getRuntime();
        file = new File("./");

        logger.info("Loaded JBoss specific statistic module. Process information: " + runtimeBean.getName());
    }

    /**
     * @param metrics update the fields passed metric object
     * @return same NodeMetric passed
     */
    public JvmMetrics collect(JvmMetrics metrics) {
        double loadAverageAdjusted = osBean.getSystemLoadAverage()  / runtime.availableProcessors();
        if (loadAverageAdjusted > 0) {
            if (loadAverageAdjusted > 1) {
                loadAverageAdjusted = 1;
            }
        } else {
            loadAverageAdjusted = 0;
        }

        metrics.setProcessCpuUsage(-1);
        metrics.setSystemCpuUsage(loadAverageAdjusted * 100);
        metrics.setMaxMemory(memoryBean.getHeapMemoryUsage().getCommitted() / 1000000D);
        metrics.setUsedMemory(memoryBean.getHeapMemoryUsage().getUsed() / 1000000D);
        metrics.setFreeMemory(metrics.getMaxMemory() - metrics.getUsedMemory());
        metrics.setThreadCount(threadBean.getThreadCount());
        metrics.setUsableDisk(file.getUsableSpace() / 1000000D);
        metrics.setLastUpdateTime(System.currentTimeMillis());
        return metrics;
    }
}
