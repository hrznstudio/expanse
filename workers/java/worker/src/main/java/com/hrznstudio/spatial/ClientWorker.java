package com.hrznstudio.spatial;

public class ClientWorker extends BaseWorker {
    @Override
    public String getWorkerID() {
        return "HorizonClientWorker";
    }

    public void initializeConnection() {
    }
}
