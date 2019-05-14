package com.dfn.watchdog.commons;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Unit test for Node.
 */
public class NodeTest {
    @Test
    public void testNode() {
        Node node = new Node((short) 1, NodeType.OMS, State.CONNECTED);

        Assert.assertEquals(State.CONNECTED, node.getState());
        Assert.assertEquals("OMS-1", node.getName());
        Assert.assertNotNull(node.getChannel());
        Assert.assertNotNull(node.getMetrics());
    }

    @Test
    public void testChangeState() {
        Node node = new Node((short) 1, NodeType.OMS, State.CLOSED);
        node.changeState(State.CONNECTED);
        Assert.assertEquals(State.CONNECTED, node.getState());
    }

    @Test
    public void testChangeBackupState() {
        Node node = new Node((short) 1, NodeType.OMS, State.CLOSED);
        node = node.changeBackupState(BackupState.RECOVERED);
        Assert.assertEquals(BackupState.RECOVERED, node.getBackupState());
    }

    @Test
    public void testCopyValues() {
        Node node1 = new Node((short) 1, NodeType.OMS, State.CLOSED);
        node1.updateLastHeartbeat();
        node1.changeBackupState(BackupState.FAILED);
        Node node2 = new Node((short) 2, NodeType.OMS, State.CLOSED);
        node2.copyValues(node1);

        Assert.assertEquals(node1.getBackupState(), node2.getBackupState());
        Assert.assertNotEquals(0, node2.getLastHeartbeat());
        Assert.assertNotEquals(node1.getName(), node2.getName());
    }

    @Test
    public void testCompareTo() {
        Node node1 = new Node((short) 1, NodeType.OMS, State.CLOSED);
        Node node2 = new Node((short) 2, NodeType.OMS, State.CLOSED);

        Assert.assertEquals(-1, node1.compareTo(node2));
    }

    @Test
    public void testEquals() {
        Node node1 = new Node((short) 1, NodeType.OMS, State.CLOSED);
        Node node2 = new Node((short) 2, NodeType.OMS, State.CLOSED);

        Assert.assertFalse(node1.equals(node2));
        Assert.assertFalse(node1.equals(null));
        Assert.assertTrue(node1.equals(node1));
    }

    public static void main(String[] args) {
        System.out.println("hello world");
        File file = new File("./");
        System.out.println(file.getTotalSpace() / 1000000000D);
        System.out.println(file.getFreeSpace() / 1000000000);
        System.out.println(file.getUsableSpace() / 1000000000);
    }
}
