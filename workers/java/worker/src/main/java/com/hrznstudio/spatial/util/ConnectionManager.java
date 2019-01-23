package com.hrznstudio.spatial.util;

import improbable.worker.*;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {
    private static ScheduledThreadPoolExecutor asyncExecutor = new ScheduledThreadPoolExecutor(1);
    private static ScheduledFuture future;
    private static Connection connection;
    private static boolean isConnected;
    private static Dispatcher dispatcher;
    private static EntityId playerId;
    private static ConnectionStatus connectionStatus;

    public static Connection getConnection() {
        return connection;
    }

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }

    public static EntityId getPlayerId() {
        return playerId;
    }

    public static ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public static void connect() {
        future = asyncExecutor.schedule(()-> {
            connection = getConnection("HorizonClientWorker" + UUID.randomUUID(), "localhost", 22000);
            isConnected = connection.isConnected();

            if (isConnected) {
                System.out.println("Successfully connected to SpatialOS");
                connectionStatus = ConnectionStatus.CONNECTED;
                dispatcher = new Dispatcher();
            } else {
                connectionStatus = ConnectionStatus.DISCONNECTED;
                System.out.println("Failed to connect to SpatialOS");
                return;
            }

            new Thread(ConnectionManager::runEventLoop).start();

            dispatcher.onCreateEntityResponse(new Callback<Ops.CreateEntityResponse>() {
                @Override
                public void call(Ops.CreateEntityResponse argument) {
                    if (argument.statusCode == StatusCode.SUCCESS) {
                        playerId = argument.entityId.get();
                    }
                }
            });
            dispatcher.onDisconnect(new Callback<Ops.Disconnect>() {
                @Override
                public void call(Ops.Disconnect argument) {
                    connectionStatus = ConnectionStatus.DISCONNECTED;
                }
            });
        }, 0, TimeUnit.SECONDS);
    }


    private static Connection getConnection(String workerId, String hostname, int port) {
        connectionStatus = ConnectionStatus.CONNECTING;
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
