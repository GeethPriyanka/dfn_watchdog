package com.dfn.watchdog.commons;

/**
 * Enum class holds the states of the components.
 */
public enum State {
    OPEN,
    INITIALIZING,
    CONNECTING,
    CONNECTED,
    SUSPENDED,
    LEAVING,
    FAILED,
    CLOSED,
    EOD,
    EOD_PRIMARY,
    UNKNOWN
}