package com.dfn.watchdog.commons.exceptions;

import com.dfn.watchdog.commons.State;

/**
 * Illegal State transition.
 */
public class IllegalStateTransitionException extends Exception {
    public IllegalStateTransitionException(String message) {
        super(message);
    }
    public IllegalStateTransitionException(State oldState, State newState) {
        super("Cannot transit from " + oldState + " to " + newState);
    }
}
