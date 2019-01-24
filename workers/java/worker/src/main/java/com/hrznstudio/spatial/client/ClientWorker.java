package com.hrznstudio.spatial.client;

import com.hrznstudio.spatial.WorkerService;

public class ClientWorker implements WorkerService {
    @Override
    public String getWorkerID() {
        return "HorizonClientWorker";
    }

    @Override
    public void start() {
        // Wont be run
    }

    public void initializeConnection() {
    }

    public void onConnectionFailure() {
    }
}
