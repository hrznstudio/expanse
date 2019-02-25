package com.hrznstudio.spatial.worker;

import com.hrznstudio.spatial.util.ConnectionManager;
import improbable.worker.Connection;
import improbable.worker.Dispatcher;
import improbable.worker.Ops;
import improbable.worker.View;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base worker class providing login and {@link Dispatcher} support
 *
 * @param <D> the dispatcher implementation to use with this worker
 */
abstract public class BaseWorker<D extends Dispatcher> implements WorkerService {

    private final Supplier<D> dispatcherProvider;
    private String name;
    private Logger logger;
    private D dispatcher;

    /**
     * @param dispatcherProvider a provider for this worker's dispatcher implementation
     */
    protected BaseWorker(@Nonnull final Supplier<D> dispatcherProvider) {
        Objects.requireNonNull(dispatcherProvider, "dispatcherProvider must not be null");
        this.dispatcherProvider = dispatcherProvider;
        setName(makeName());
    }

    @Override
    public void start() {
        logger.info(() -> "Starting login process");
        ConnectionManager.connect(name, getWorkerType(), this.dispatcher = dispatcherProvider.get());
        ConnectionManager.setConnectionCallback(() -> {
            logger.info(() -> "Logged in");
            ConnectionManager.getDispatcher().onDisconnect(this::onDisConnected);
            this.onConnected();
        });
    }

    /**
     * Connection callback
     */
    abstract protected void onConnected();

    /**
     * Disconnection callback
     *
     * @param reason cause of disconnection
     */
    protected void onDisConnected(@Nonnull final Ops.Disconnect reason) {
        logger.info(() -> String.format("Disconnected (%s: %s)", reason.connectionStatusCode, reason.reason));
        this.dispatcher = null;
    }

    /**
     * @return this worker's current unique name
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Update this worker's name. This will also update the logger
     *
     * @param name new name for this worker
     */
    protected void setName(@Nonnull final String name) {
        Objects.requireNonNull(name, "name must not be null");
        this.name = name;
        this.logger = LogManager.getLogger(name);
    }

    /**
     * @return logger in use by this worker
     */
    @Nonnull
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return this worker's current dispatcher
     */
    @Nullable
    public D getDispatcher() {
        return dispatcher;
    }
    public Connection getConnection() {
        return ConnectionManager.getConnection();
    }

    /**
     * Base worker class providing login and {@link Dispatcher} support trough the default {@link Dispatcher} implementation
     */
    public static abstract class BaseDispatcherWorker extends BaseWorker<Dispatcher> {
        protected BaseDispatcherWorker() {
            super(Dispatcher::new);
        }
    }

    /**
     * Base worker class providing login and {@link Dispatcher} support trough the {@link View} implementation
     */
    public static abstract class BaseViewWorker extends BaseWorker<View> {
        protected BaseViewWorker() {
            super(View::new);
        }
    }
}
