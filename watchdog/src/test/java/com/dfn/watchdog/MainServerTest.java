package com.dfn.watchdog;

import com.dfn.watchdog.commons.exceptions.InvalidConfigurationError;
import org.junit.Test;

/**
 * Unit tests for monitor handler.
 */
public class MainServerTest {
    String configFilePath = "./src/test/resources/watchdog-test-main-server.yml";

    @Test(expected = InvalidConfigurationError.class)
    public void testMainException() {
        MainServer.main(new String[]{""});
    }

    @Test
    public void testMain() {
        MainServer.main(new String[]{configFilePath});
    }
}
