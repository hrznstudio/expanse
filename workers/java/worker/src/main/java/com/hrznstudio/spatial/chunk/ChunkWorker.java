package com.hrznstudio.spatial.chunk;

import com.hrznstudio.spatial.WorkerService;

public class ChunkWorker implements WorkerService {
    @Override
    public String getWorkerID() {
        return "ChunkWorker";
    }

    @Override
    public void start() {

    }
}
