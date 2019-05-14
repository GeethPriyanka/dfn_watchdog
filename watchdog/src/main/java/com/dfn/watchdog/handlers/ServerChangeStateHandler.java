package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.cluster.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Handles the states changes of the cluster.
 * <p>
 * Node state and it's backup state changes are handled.
 */
public class ServerChangeStateHandler extends SimpleChannelInboundHandler<ChangeStateMessage> {
    private static final Logger logger = LogManager.getLogger(ServerChangeStateHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ServerChangeStateHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChangeStateMessage message) throws Exception {
        if (message instanceof ChangeState) {
            Node node = ((ChangeState) message).getNode()
                    .setChannel(ctx.channel())
                    .setIpAddress(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
            logger.info("Change view message received from " + node);

            if (node.getState() == State.CONNECTING) {
                node.changeState(State.CONNECTED);
                ctx.writeAndFlush(new ChangeState(node));
            }

            View view = WatchdogServer.INSTANCE.getView();
            view.addNode(node);
            view.refreshChannels();
            WatchdogServer.INSTANCE.broadcastMessage(new ChangeView(view));
            logger.info(view);
        } else if (message instanceof BackupReady) {
            Node node = ((BackupReady) message).getNode();
            View view = WatchdogServer.INSTANCE.getView();
            logger.info("Backup is ready for the node: " + node + " Backup node: " + view.getNode(ctx.channel()));
            view.getNode(node).changeBackupState(node.getBackupState());
            view.incrementVersion();
            view.refreshChannels();
            WatchdogServer.INSTANCE.broadcastMessage(new ChangeView(view));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel inactive, remote address: " + ctx.channel().remoteAddress());
        Node node = WatchdogServer.INSTANCE.getView().getNode(ctx.channel());
        if (node != null) {
            node.getMetrics().addDisconnect();
            node.changeState(State.SUSPENDED);
            WatchdogServer.INSTANCE.broadcastMessage(new ChangeView(WatchdogServer.INSTANCE.getView()));
        } else {
            logger.warn("Tearing the connection of possible duplicate client @" + ctx.channel().remoteAddress());
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Channel caught an exception: ", cause);
        View view = WatchdogServer.INSTANCE.getView();
        Node node = view.getNode(ctx.channel());
        if (node != null)
            node.changeState(State.SUSPENDED);
        ctx.close();
        view.refreshChannels();
        view.populateLinks();
        WatchdogServer.INSTANCE.routeToMonitor(new ChangeLinks(view.getLinks()));
        WatchdogServer.INSTANCE.broadcastMessage(new ChangeView(view));
    }
}