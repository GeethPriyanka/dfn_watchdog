package com.dfn.watchdog.commons.messages;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.messages.monitoring.LinkStatus;
import com.dfn.watchdog.commons.messages.monitoring.NodeMetrics;
import com.dfn.watchdog.commons.util.Formatters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Unit tests for system metrics.
 */
public class LinkStatusTest {

    @Test
    public void testStringConstruction() {
        LinkStatus linkStatus = new LinkStatus("GATEWAY-1", "OMS-2", State.CONNECTED);
        Assert.assertEquals("GATEWAY-1 CONNECTED OMS-2", linkStatus.toString());
    }

}