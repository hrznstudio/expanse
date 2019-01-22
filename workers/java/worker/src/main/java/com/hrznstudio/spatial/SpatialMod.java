package com.hrznstudio.spatial;

import improbable.Coordinates;
import improbable.Position;
import improbable.PositionData;
import improbable.collections.Option;
import improbable.worker.*;
import minecraft.entity.*;
import minecraft.inventory.Inventory;
import minecraft.inventory.InventoryData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Mod(modid = "spatial")
@Mod.EventBusSubscriber
public class SpatialMod {
    static ScheduledThreadPoolExecutor asyncExecutor = new ScheduledThreadPoolExecutor(1);
    private static ScheduledFuture future;
    public static Connection connection;
    private static boolean isConnected;
    private static Dispatcher dispatcher;
    private static EntityId playerId;

    public static EntityId getPlayerId() {
        return playerId;
    }

    public static boolean isConnectedToSpatial() {
        return isConnected;
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        connection = getConnection("HorizonClientWorker" + UUID.randomUUID(), "localhost", 22000);
        //TODO: this should be threaded in future
        isConnected = connection.isConnected();

        if (isConnected) {
            System.out.println("Successfully connected to SpatialOS");
            dispatcher = new Dispatcher();
        } else {
            System.out.println("Failed to connect to SpatialOS");
            throw new RuntimeException();
        }

        new Thread(SpatialMod::runEventLoop).start();

        dispatcher.onCreateEntityResponse(new Callback<Ops.CreateEntityResponse>() {
            @Override
            public void call(Ops.CreateEntityResponse argument) {
                if (argument.statusCode == StatusCode.SUCCESS) {
                    playerId = argument.entityId.get();
                }
            }
        });
        dispatcher.onRemoveEntity(new Callback<Ops.RemoveEntity>() {
            @Override
            public void call(Ops.RemoveEntity argument) {
                if(argument.entityId==playerId)
                    connection.sendCreateEntityRequest(createPlayerEntity(), Option.empty(), Option.empty());
            }
        });
        RequestId<CreateEntityRequest> requestId = connection.sendCreateEntityRequest(createPlayerEntity(), Option.empty(), Option.empty());


    }


    public static Entity createPlayerEntity() {
        EntityBuilder builder = new EntityBuilder("Player");
        builder.addComponent(
                Position.COMPONENT,
                new PositionData(new Coordinates(0, 0, 0)),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Player.COMPONENT,
                new PlayerData(new GameProfile(UUID.randomUUID().toString()), false, false),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Health.COMPONENT,
                new HealthData(20, 20),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Food.COMPONENT,
                new FoodData(20, 20),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Experience.COMPONENT,
                new ExperienceData(0),
                CommonWorkerRequirements.getEntityWorkers()
        );
        builder.addComponent(
                Inventory.COMPONENT,
                new InventoryData(Collections.emptyList()),
                CommonWorkerRequirements.getEntityWorkers()
        );
        return builder.build();
    }

    private static Connection getConnection(String workerId, String hostname, int port) {
        ConnectionParameters parameters = new ConnectionParameters();
        parameters.workerType = "HorizonClientWorker";
        parameters.network = new NetworkParameters();
        parameters.network.connectionType = NetworkConnectionType.Tcp;
        parameters.network.useExternalIp = false;

        return Connection.connectAsync(hostname, port, workerId, parameters).get();
    }

    private static final int UPDATES_PER_SECOND = 20;

    private static void runEventLoop() {
        java.time.Duration maxWait = java.time.Duration.ofMillis(Math.round(1000.0 / UPDATES_PER_SECOND));
        while (isConnected) {
            long startTime = System.nanoTime();
            OpList opList = connection.getOpList(0);
            dispatcher.process(opList);

            long stopTime = System.nanoTime();
            java.time.Duration waitFor = maxWait.minusNanos(stopTime - startTime);
            try {
                Thread.sleep(Math.max(waitFor.toMillis(), 0));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
}
