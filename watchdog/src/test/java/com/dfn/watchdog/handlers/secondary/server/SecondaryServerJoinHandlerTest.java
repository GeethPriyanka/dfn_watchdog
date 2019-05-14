package com.dfn.watchdog.handlers.secondary.server;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.messages.secondary.SecondaryJoinMessage;
import com.dfn.watchdog.handlers.MockChannelHandlerContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({WatchdogServer.class})
@PowerMockIgnore("javax.management.*")
public class SecondaryServerJoinHandlerTest {

    @Test
    public void channelRead0Test() throws Exception {
        MockChannelHandlerContext ctx = new MockChannelHandlerContext();
        SecondaryServerJoinHandler secondaryServerJoinHandler = new SecondaryServerJoinHandler();
        secondaryServerJoinHandler.channelRead0(ctx, new SecondaryJoinMessage());
    }
}
