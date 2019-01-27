package com.hrznstudio.spatial.client;

import com.hrznstudio.spatial.BaseWorker;
import com.hrznstudio.spatial.client.vanillawrappers.SpatialNetworkManager;
import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.util.ConnectionStatus;
import improbable.worker.EntityId;
import improbable.worker.Ops;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;

import javax.annotation.Nonnull;
import java.util.Collections;

public final class HorizonClientWorker extends BaseWorker<ClientView> {
    private EntityId playerId;
    private NetHandlerPlayClient netHandlerPlayClient;
    private NetworkManager networkManager;
    private GuiMainMenu guiMainMenu;

    public HorizonClientWorker() {
        super(ClientView::new);
    }

    public EntityId getPlayerId() {
        return playerId;
    }

    @Override
    public void start() {
        Minecraft mc = Minecraft.getMinecraft();
        //noinspection ConstantConditions
        if (mc == null) throw new IllegalStateException("Client worker should never be started this way");
        guiMainMenu = new GuiMainMenu();

        this.setName(makeName());
        super.start();
    }

    public void stop() {
        ConnectionManager.disconnect();
    }

    public ConnectionStatus getConnectionStatus() {
        return ConnectionManager.getConnectionStatus();
    }

    @Override
    protected void onConnected() {
        Minecraft mc = Minecraft.getMinecraft();
        networkManager = new SpatialNetworkManager(this);
        netHandlerPlayClient = new NetHandlerPlayClient(mc, guiMainMenu, networkManager, mc.getSession().getProfile());
        FMLClientHandler.instance().setPlayClient(netHandlerPlayClient);
        NetworkDispatcher.allocAndSet(networkManager);

        mc.addScheduledTask(() -> {
            netHandlerPlayClient.handleJoinGame(new SPacketJoinGame(
                    0,
                    GameType.CREATIVE,
                    false,
                    0,
                    EnumDifficulty.NORMAL,
                    9001,
                    WorldType.FLAT,
                    false
            ));
            netHandlerPlayClient.handlePlayerPosLook(new SPacketPlayerPosLook(
                    0, 3, 0, 0, 0, Collections.emptySet(), -1
            ));
        });
    }

    @Override
    protected void onDisConnected(@Nonnull final Ops.Disconnect reason) {
        super.onDisConnected(reason);
        WorldClient wc = Minecraft.getMinecraft().world;
        if (wc != null) wc.sendQuittingDisconnectingPacket();
    }
}
