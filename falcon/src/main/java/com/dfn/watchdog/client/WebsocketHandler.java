package com.dfn.watchdog.client;

import com.dfn.watchdog.client.util.ClientConstants;
import com.dfn.watchdog.client.util.DataSupplier;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Push messages to web.
 */
public class WebsocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger logger = LogManager.getLogger(WebsocketHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {}", ctx.channel(), request);

            WatchdogClient.INSTANCE.broadcastToWeb(getShowKibanaMessage());
            WatchdogClient.INSTANCE.broadcastToWeb(ClientConstants.toJsonMetric(WatchdogClient.INSTANCE.getView()));
            WatchdogClient.INSTANCE.broadcastToWeb(WatchdogClient.INSTANCE.getSystemMetrics().toJson());
            WatchdogClient.INSTANCE.broadcastToWeb(DataSupplier.getViewForTree(WatchdogClient.INSTANCE.getView()));
            WatchdogClient.INSTANCE.broadcastToWeb(DataSupplier.getViewForBlocks(
                    WatchdogClient.INSTANCE.getView(), WatchdogClient.INSTANCE.getLinks()));

        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        WatchdogClient.INSTANCE.addWebSession(ctx.channel());
        logger.info("Channel Active callback");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        WatchdogClient.INSTANCE.removeWebSession(ctx.channel());
        logger.info("Channel Inactive callback");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Channel caught an exception", cause);
        ctx.close();
    }

    private String getShowKibanaMessage() {
        return "{\"messageType\" : \"showKibanaDashboard\", \"show\" :"
                + WatchdogClient.INSTANCE.getProperties().isShowKibanaDashboard() + "}";
    }
}