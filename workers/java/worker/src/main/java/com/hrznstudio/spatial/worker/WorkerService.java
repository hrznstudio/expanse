package com.hrznstudio.spatial.worker;

import java.util.UUID;

/**
 * SpatialOS Worker.
 * Implement this interface for the service loader to pick your worker up.
 */
public interface WorkerService {
    String NAME_SEPARATOR = "-";

    /**
     * @return new unique name for this worker
     */
    default String makeName() {
        return getWorkerType() + NAME_SEPARATOR + UUID.randomUUID();
    }

    /**
     * @return this worker's type, used to match with the launch profile
     */
    default String getWorkerType() {
        return getClass().getSimpleName();
    }

    /**
     * Start execution
     */
    void start();
}
