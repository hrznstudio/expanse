package com.hrznstudio.spatial.api;

import improbable.worker.EntityId;

public interface ISpatialEntity {
    EntityId getSpatialId();
    void setSpatialId(EntityId id);
}
