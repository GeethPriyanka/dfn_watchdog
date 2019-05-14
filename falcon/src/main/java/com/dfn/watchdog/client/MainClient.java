package com.dfn.watchdog.client;

/**
 * Main class to fire up the client application.
 */
public class MainClient {
    public static void main(String[] args) {
        if (args.length > 0) {
            WatchdogClient.INSTANCE.configure(args[0]).run();
        } else {
            WatchdogClient.INSTANCE.configure(null).run();
        }
    }
}
