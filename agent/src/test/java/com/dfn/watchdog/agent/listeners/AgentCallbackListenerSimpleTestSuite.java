package com.dfn.watchdog.agent.listeners;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AgentCallbacks unit testing.
 */
public class AgentCallbackListenerSimpleTestSuite {
    AgentCallbackListenerSimple listener;

    @Before
    public void setup() {
        listener = new AgentCallbackListenerSimple();
    }

    @Test
    public void testInitializing() {
        Assert.assertTrue(listener.initializing(State.CLOSED));
    }

    @Test
    public void testConnecting() {
        Assert.assertTrue(listener.connecting(State.CLOSED));
    }

    @Test
    public void testConnected() {
        Assert.assertTrue(listener.connected(State.CLOSED));
    }

    @Test
    public void testSuspended() {
        Assert.assertTrue(listener.suspended(State.CLOSED));
    }

    @Test
    public void testLeaving() {
        Assert.assertTrue(listener.leaving(State.CLOSED));
    }

    @Test
    public void testFailed() {
        Assert.assertTrue(listener.failed(State.CLOSED));
    }

    @Test
    public void testClosed() {
        Assert.assertTrue(listener.closed(State.CLOSED));
    }

    @Test
    public void testBackupRecovering() {
        Assert.assertTrue(listener
                .backupRecovering(new Node((short) 1, NodeType.GATEWAY, State.CLOSED)));
    }

    @Test
    public void testBackupRecovered() {
        Assert.assertTrue(listener
                .backupRecovered(new Node((short) 1, NodeType.GATEWAY, State.CLOSED)));
    }

    @Test
    public void testBackupRecoveryFailed() {
        Assert.assertTrue(listener
                .backupRecoveryFailed(new Node((short) 1, NodeType.GATEWAY, State.CLOSED)));
    }

    @Test
    public void testNext() {
        short next = listener.next(12345);
        Assert.assertEquals(0, next);
    }

    @Test
    public void testGetBackup() {
        Node next = listener.getBackup((short) 0, NodeType.OMS);
        Assert.assertEquals(1, next.getId());
    }

    @Test
    public void testBlockCustomer() {
        listener.blockCustomer(123);
    }
}
