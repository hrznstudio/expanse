package com.hrznstudio.spatial.client;

import com.hrznstudio.spatial.WorkerService;
import com.hrznstudio.spatial.client.vanillawrappers.SpatialNetworkManager;
import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.util.ConnectionStatus;
import improbable.worker.EntityId;
import improbable.worker.View;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;

import java.util.Collections;
import java.util.UUID;

public class ClientWorker implements WorkerService {
    private EntityId playerId;
    private View view;
    private NetHandlerPlayClient netHandlerPlayClient;
    private NetworkManager networkManager;
    private GuiMainMenu guiMainMenu;

    public EntityId getPlayerId() {
        return playerId;
    }

    @Override
    public String getWorkerID() {
        return "HorizonClientWorker";
    }

    @Override
    public void start() {
        Minecraft mc = Minecraft.getMinecraft();
        //noinspection ConstantConditions
        if (mc == null) throw new IllegalStateException("Client worker should never be started this way");
        guiMainMenu = new GuiMainMenu();
        ConnectionManager.connect(getWorkerID() + '$' + mc.getSession().getProfile().getId() + "$" + UUID.randomUUID(), true);
        ConnectionManager.setConnectionCallback(this::initializeConnection);
    }

    public void stop() {
        ConnectionManager.disconnect();
        this.view = null;
    }

    public ConnectionStatus getConnectionStatus() {
        return ConnectionManager.getConnectionStatus();
    }

    private void initializeConnection() {
        view = ConnectionManager.getView();
        view.onDisconnect(argument -> onConnectionFailure());

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

    public void onConnectionFailure() {
        // TODO
    }
}
