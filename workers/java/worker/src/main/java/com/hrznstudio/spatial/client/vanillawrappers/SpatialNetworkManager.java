package com.hrznstudio.spatial.client.vanillawrappers;

import com.hrznstudio.spatial.client.HorizonClientWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.*;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;

public class SpatialNetworkManager extends NetworkManager {
    private final HorizonClientWorker clientWorker;
    private Logger logger = LogManager.getLogger();

    public SpatialNetworkManager(HorizonClientWorker clientWorker) {
        super(EnumPacketDirection.CLIENTBOUND);
        this.clientWorker = clientWorker;
        channel = new EmbeddedChannel();
    }

    @Override
    public void channelActive(ChannelHandlerContext p_channelActive_1_) throws Exception {
        logger.info(() -> "SpatialNetworkManager#channelActive not implemented");
    }

    @Override
    public void setConnectionState(EnumConnectionState newState) {
        logger.info(() -> "SpatialNetworkManager#setConnectionState not implemented");
    }

    @Override
    public void channelInactive(ChannelHandlerContext p_channelInactive_1_) throws Exception {
        logger.info(() -> "SpatialNetworkManager#channelInactive not implemented");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) throws Exception {
        logger.info(() -> "SpatialNetworkManager#exceptionCaught not implemented");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_) throws Exception {
        logger.info(() -> "SpatialNetworkManager#channelRead0 not implemented");
    }

    @Override
    public void setNetHandler(INetHandler handler) {
        logger.info(() -> "SpatialNetworkManager#setNetHandler not implemented");
    }

    @Override
    public void sendPacket(Packet<?> packetIn) {
        logger.info(() -> "SpatialNetworkManager#sendPacket not implemented (received " + packetIn.getClass() + ")");
    }

    @Override
    public void sendPacket(Packet<?> packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>... listeners) {
        logger.info(() -> "SpatialNetworkManager#sendPacket(...) not implemented");
    }

    @Override
    public void processReceivedPackets() {
//        logger.info(() -> "SpatialNetworkManager#processReceivedPackets not implemented");
    }

    @Override
    public SocketAddress getRemoteAddress() {
        throw new IllegalStateException(); // Called on server only
    }

    @Override
    public void handleDisconnection() {
        super.handleDisconnection();
        logger.warn(() -> "NetworkManager disconnected");
    }

    @Override
    public void closeChannel(ITextComponent message) {
        super.closeChannel(message);
        clientWorker.stop();
    }
}
