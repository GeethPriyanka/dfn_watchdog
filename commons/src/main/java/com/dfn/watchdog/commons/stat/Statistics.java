package com.dfn.watchdog.commons.stat;

import com.dfn.watchdog.commons.messages.monitoring.JvmMetrics;

/**
 * Collect statistics of the system.
 */
public interface Statistics {
    JvmMetrics collect(JvmMetrics metrics);
}
