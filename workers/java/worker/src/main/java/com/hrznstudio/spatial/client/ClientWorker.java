package com.hrznstudio.spatial.client;

import com.hrznstudio.spatial.util.BaseWorker;

public class ClientWorker extends BaseWorker {
    @Override
    public String getWorkerID() {
        return "HorizonClientWorker";
    }

    public void initializeConnection() {
    }

    public void onConnectionFailure() {
    }
}
