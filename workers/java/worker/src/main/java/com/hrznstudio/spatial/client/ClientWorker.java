package com.hrznstudio.spatial.client;

import com.hrznstudio.spatial.WorkerService;
import com.hrznstudio.spatial.util.ConnectionManager;
import improbable.worker.Dispatcher;
import improbable.worker.EntityId;
import improbable.worker.StatusCode;

public class ClientWorker implements WorkerService {
    private EntityId playerId;

    public EntityId getPlayerId() {
        return playerId;
    }
    @Override
    public String getWorkerID() {
        return "HorizonClientWorker";
    }

    @Override
    public void start() {
        throw new IllegalStateException("Client worker should never be started this way");
    }

    public void initializeConnection() {
        Dispatcher dispatcher = ConnectionManager.getDispatcher();
        dispatcher.onCreateEntityResponse(argument -> {
            if (argument.statusCode == StatusCode.SUCCESS) {
                playerId = argument.entityId.get();
            }
        });
        dispatcher.onDisconnect(argument -> onConnectionFailure());
    }

    public void onConnectionFailure() {
    }
}
