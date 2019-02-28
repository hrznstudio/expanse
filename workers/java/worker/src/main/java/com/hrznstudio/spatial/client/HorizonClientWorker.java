package com.hrznstudio.spatial.client;

import com.hrznstudio.spatial.client.vanillawrappers.SpatialNetworkManager;
import com.hrznstudio.spatial.util.CommonWorkerRequirements;
import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.util.ConnectionStatus;
import com.hrznstudio.spatial.util.EntityBuilder;
import com.hrznstudio.spatial.worker.BaseWorker;
import improbable.*;
import improbable.collections.Option;
import improbable.worker.*;
import minecraft.entity.*;
import minecraft.player.*;
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
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public final class HorizonClientWorker extends BaseWorker<ClientView> {
    private volatile EntityId playerId;
    private NetHandlerPlayClient netHandlerPlayClient;
    private NetworkManager networkManager;
    private GuiMainMenu guiMainMenu;

    public HorizonClientWorker() {
        super(ClientView::new);
    }

    @Nullable
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
        Connection connection = ConnectionManager.getConnection();
        Dispatcher dispatcher = getDispatcher();
        final Option<Integer> timeoutMillis = Option.of(500);

        // Reserve an entity ID.
        RequestId<ReserveEntityIdsRequest> entityIdReservationRequestId = connection.sendReserveEntityIdsRequest(1, timeoutMillis);
        // When the reservation succeeds, create an entity with the reserved ID.

        AtomicReference<RequestId<CreateEntityRequest>> createEntityRequestRequestId = new AtomicReference<>();
        dispatcher.onReserveEntityIdsResponse(op -> {
            if (op.requestId.equals(entityIdReservationRequestId) && op.statusCode == StatusCode.SUCCESS) {
                EntityBuilder builder = new EntityBuilder("Player");
                builder.addComponent(Position.COMPONENT, new improbable.PositionData(new Coordinates(8, 20, 8)), // TODO: use position from server
                        CommonWorkerRequirements.createWorkerRequirementSet("entity_worker")
                );
                builder.addComponent(
                        PlayerInfo.COMPONENT,
                        new PlayerInfoData(new GameProfile(
                                Minecraft.getMinecraft().getSession().getPlayerID(), Minecraft.getMinecraft().getSession().getUsername()
                        )
                        ),
                        CommonWorkerRequirements.createWorkerRequirementSet("entity_worker")
                );
                builder.addComponent(
                        PlayerInput.COMPONENT,
                        new PlayerInputData(new Vector3f(0, 0, 0),new Vector3f(0, 0, 0), false, false
                        ),
                        new WorkerRequirementSet(Collections.singletonList(new WorkerAttributeSet(Collections.singletonList("workerId:" + this.getName()))))
                );
                builder.addComponent(
                        PlayerConnection.COMPONENT,
                        new PlayerConnectionData(),
                        new WorkerRequirementSet(Collections.singletonList(new WorkerAttributeSet(Collections.singletonList("workerId:" + this.getName()))))
                );
                builder.addComponent(
                        Motion.COMPONENT,
                        new MotionData(new Vector3f(0, 0, 0)),
                        CommonWorkerRequirements.createWorkerRequirementSet("entity_worker")
                );
                builder.addComponent(
                        Rotation.COMPONENT,
                        new RotationData(0, 0),
                        new WorkerRequirementSet(Collections.singletonList(new WorkerAttributeSet(Collections.singletonList("workerId:" + this.getName()))))
                );
                builder.addComponent(
                        WorldEntity.COMPONENT,
                        new WorldEntityData(),
                        CommonWorkerRequirements.createWorkerRequirementSet("entity_worker")
                );
                createEntityRequestRequestId.set(connection.sendCreateEntityRequest(builder.build(), op.firstEntityId, timeoutMillis));
            }
        });
        Minecraft mc = Minecraft.getMinecraft();
        dispatcher.onCreateEntityResponse(argument -> {
            if (argument.requestId.equals(createEntityRequestRequestId.get()) && argument.statusCode == StatusCode.SUCCESS) {
                playerId = argument.entityId.get();
            } else {
                stop();
            }
        });
        mc.addScheduledTask(() -> {
            networkManager = new SpatialNetworkManager(this);
            netHandlerPlayClient = new NetHandlerPlayClient(mc, guiMainMenu, networkManager, mc.getSession().getProfile());
            FMLClientHandler.instance().setPlayClient(netHandlerPlayClient);
            NetworkDispatcher.allocAndSet(networkManager);
            networkManager.setNetHandler(netHandlerPlayClient);
            netHandlerPlayClient.handleJoinGame(new SPacketJoinGame(
                    0,
                    GameType.CREATIVE,
                    false,
                    0,
                    EnumDifficulty.NORMAL,
                    1000,
                    WorldType.FLAT,
                    false
            ));
            netHandlerPlayClient.handlePlayerPosLook(new SPacketPlayerPosLook(
                    8, 60, 8, 0, 0, Collections.emptySet(), -1
            )); // TODO: use position from server
        });
    }

    @Override
    protected void onDisConnected(@Nonnull final Ops.Disconnect reason) {
        super.onDisConnected(reason);
        WorldClient wc = Minecraft.getMinecraft().world;
        if (wc != null) wc.sendQuittingDisconnectingPacket();
    }

    public NetHandlerPlayClient getNetHandlerPlayClient() {
        return netHandlerPlayClient;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }
}
