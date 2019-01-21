package com.hrznstudio.spatial;

import improbable.Coordinates;
import improbable.Position;
import improbable.PositionData;
import improbable.collections.Option;
import improbable.worker.*;
import minecraft.entity.*;
import minecraft.inventory.Inventory;
import minecraft.inventory.InventoryData;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Bootstrap {
    static ScheduledThreadPoolExecutor asyncExecutor = new ScheduledThreadPoolExecutor(1);
    private static ScheduledFuture future;
    private static Connection connection;
    private static boolean isConnected;
    private static Dispatcher dispatcher;

    public static void main(String[] arr) {
        future = asyncExecutor.schedule(new Runnable() {
            public void run() {
                connection = getConnection("HorizonClientWorker" + UUID.randomUUID(), "localhost", 22000);
                isConnected = connection.isConnected();

                if (isConnected) {
                    System.out.println("Successfully connected to SpatialOS");
                    dispatcher = new Dispatcher();
                    connection.sendCreateEntityRequest(
                            createPlayerEntity(),
                            Option.empty(),
                            Option.empty()
                    );
                } else {
                    System.out.println("Failed to connect to SpatialOS");
                }
            }
        }, 1, TimeUnit.SECONDS);
        while(true) {
            if(isConnectedToSpatialOS()) {
                try {
                    update();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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
                new PlayerData(new GameProfile(UUID.randomUUID().toString()), false,false),
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
    public static boolean isConnectionAttemptCompleted() {
        return future.isDone();
    }

    public static boolean isConnectedToSpatialOS() {
        return isConnectionAttemptCompleted() && isConnected;
    }

    private static Connection getConnection(String workerId, String hostname, int port) {
        ConnectionParameters parameters = new ConnectionParameters();
        parameters.workerType = "HorizonJavaWorker";
        parameters.network = new NetworkParameters();
        parameters.network.connectionType = NetworkConnectionType.Tcp;
        parameters.network.useExternalIp = false;

        return Connection.connectAsync(hostname, port, workerId, parameters).get();
    }
    public static void update() throws InterruptedException {
        try (OpList opList = connection.getOpList(0 /* non-blocking */)) {
            // Invoke user-provided callbacks.
            dispatcher.process(opList);
            Thread.sleep(30);
        }
    }
}