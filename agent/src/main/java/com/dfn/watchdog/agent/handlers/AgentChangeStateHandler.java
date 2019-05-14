package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.agent.listeners.AgentCallbackListener;
import com.dfn.watchdog.agent.util.AgentWriteListener;
import com.dfn.watchdog.commons.BackupState;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.messages.cluster.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the states changes of the node.
 * <p>
 * Node state and it's backup state changes are handled.
 */
public class AgentChangeStateHandler extends SimpleChannelInboundHandler<ChangeStateMessage> {
    private static final Logger logger = LoggerFactory.getLogger(AgentChangeStateHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentChangeStateHandler() {
        super(false);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ChangeStateMessage message) throws Exception {
        logger.debug("ChangeView message received: {}", message);
        if (message instanceof ChangeState) {
            ChangeState changeState = (ChangeState) message;
            WatchdogAgent.INSTANCE.getNode().changeState(changeState.getNode().getState());
            logger.info(WatchdogAgent.INSTANCE.getNode().toString());
        } else if (message instanceof ChangeView) {
            ChangeView changeView = (ChangeView) message;
            if (changeView.getView().getServerState() != State.CONNECTED) {
                WatchdogAgent.INSTANCE.purgeClientRoutes();
            }
            WatchdogAgent.INSTANCE.installView(changeView.getView());
            logger.info(WatchdogAgent.INSTANCE.getView().toString());
        } else if (message instanceof BackupNotify) {
            BackupNotify backupNotify = (BackupNotify) message;
            AgentCallbackListener listener = WatchdogAgent.INSTANCE.getListener();
            Node closedNode = backupNotify.getNode();
            Node backupNode = listener.getBackup(closedNode.getId(), closedNode.getType());

            if (backupNode != null && backupNode.getName().equals(WatchdogAgent.INSTANCE.getNode().getName())) {
                ctx.channel().eventLoop().parent().submit(() -> listener.backupRecovering(closedNode))
                        .addListener(future -> {
                            if ((Boolean) future.get()) {
                                closedNode.changeBackupState(BackupState.RECOVERED);
                                BackupReady backupMessage = new BackupReady(closedNode);
                                ctx.writeAndFlush(backupMessage)
                                        .addListener(new AgentWriteListener(backupMessage, ctx.channel()));
                            } else {
                                closedNode.changeBackupState(BackupState.FAILED);
                                BackupFailed backupMessage = new BackupFailed(closedNode);
                                ctx.writeAndFlush(backupMessage)
                                        .addListener(new AgentWriteListener(backupMessage, ctx.channel()));
                            }
                        });
            }
        } else if (message instanceof PurgeMappings) {
            String nodeTypeStr = ((PurgeMappings) message).getNodeType();
            NodeType nodeType = nodeTypeStr == null ? NodeType.OMS : NodeType.valueOf(nodeTypeStr);
            if (nodeType == NodeType.OMS) {
                WatchdogAgent.INSTANCE.purgeClientRoutes(((PurgeMappings) message).getNodeId());
            } else if (nodeType == NodeType.AURA) {
                WatchdogAgent.INSTANCE.purgeClientRoutesAura(((PurgeMappings) message).getNodeId());
            }
        } else {
            logger.warn("Unknown message received: {}", message);
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        logger.warn("Channel disconnected with the watchdog server. Changing state to Suspended");
        Node node = WatchdogAgent.INSTANCE.getNode();
        WatchdogAgent.INSTANCE.getListener().suspended(node.getState());
        node.changeState(State.SUSPENDED);
        ctx.fireChannelInactive();
    }
}
