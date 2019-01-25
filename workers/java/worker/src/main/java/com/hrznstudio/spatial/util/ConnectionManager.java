package com.hrznstudio.spatial.util;

import improbable.worker.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {
    private static ScheduledThreadPoolExecutor asyncExecutor = new ScheduledThreadPoolExecutor(1);
    private static ScheduledFuture future;
    private static Connection connection;
    private static boolean isConnected;
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class.getSimpleName());
    private static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private static Dispatcher dispatcher;
    private static Runnable connectionCallback = null;
    private static boolean useView = false;

    public static Connection getConnection() {
        return connection;
    }

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }

    public static View getView() {
        if (!useView) throw new IllegalStateException("This ConnectionManager isn't in view mode!");
        return (View) dispatcher;
    }

    public static ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public static boolean hasConnectionFinished() {
        return future.isDone();
    }

    public static void connect(final String workerName, final boolean useView) {
        ConnectionManager.useView = useView;
        future = asyncExecutor.schedule(() -> {
            connection = getConnection(workerName, "localhost", 22000);
            isConnected = connection.isConnected();

            if (isConnected) {
                logger.info("Successfully connected to SpatialOS");
                connectionStatus = ConnectionStatus.CONNECTED;
                dispatcher = useView ? new View() : new Dispatcher();
                dispatcher.onDisconnect(dc -> {
                    logger.info("Disconnected from SpatialOS");
                    isConnected = false;
                });
                if (connectionCallback != null) connectionCallback.run();
            } else {
                connectionStatus = ConnectionStatus.FAILED;
                logger.info("Failed to connect to SpatialOS");
                return;
            }

            new Thread(ConnectionManager::runEventLoop).start();


        }, 0, TimeUnit.SECONDS);
    }

    public static void connect(final String workerName) {
        connect(workerName, false);
    }

    public static void setConnectionCallback(Runnable connectionCallback) {
        ConnectionManager.connectionCallback = connectionCallback;
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
