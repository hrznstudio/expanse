package com.hrznstudio.spatial.util;

import improbable.worker.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConnectionManager {
    // Having this be a fixed thread pool serves as keep-alive until the event loop kicks in
    private static ExecutorService loginPool = Executors.newFixedThreadPool(1);
    // Using a stealing pool with a parallelism of 1 makes this behave similarly to Actors
    private static ExecutorService processingPool = Executors.newWorkStealingPool(1);
    private static Future future;
    private static Connection connection;
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

    public static void disconnect() {
        if (future != null && !future.isDone()) future.cancel(true);
        connectionStatus = ConnectionStatus.DISCONNECTED;
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispatcher = null;
    }

    public static boolean hasConnectionFinished() {
        return future.isDone();
    }

    public static void connect(final String workerName, final boolean useView) {
        if (future != null && !future.isDone() && useView != ConnectionManager.useView) future.cancel(true);
        if (future == null || future.isDone()) {
            ConnectionManager.useView = useView;
            future = loginPool.submit(() -> {
                connection = getConnection(workerName, "localhost", 22000);
                connectionStatus = connection.isConnected() ? ConnectionStatus.CONNECTED : ConnectionStatus.FAILED;

                if (isConnected()) {
                    logger.info("Successfully connected to SpatialOS");
                    dispatcher = useView ? new View() : new Dispatcher();
                    dispatcher.onDisconnect(dc -> {
                        logger.info("Disconnected from SpatialOS");
                        connectionStatus = ConnectionStatus.DISCONNECTED;
                    });
                    if (connectionCallback != null) connectionCallback.run();
                } else {
                    logger.info("Failed to connect to SpatialOS");
                    return;
                }

                new Thread(ConnectionManager::runEventLoop).start();
            });
        }
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
        while (isConnected()) {
            long startTime = System.nanoTime();
            OpList opList = connection.getOpList(0);
            processingPool.submit(() -> dispatcher.process(opList));

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

    private static boolean isConnected() {
        return connectionStatus.isConnected();
    }
}
