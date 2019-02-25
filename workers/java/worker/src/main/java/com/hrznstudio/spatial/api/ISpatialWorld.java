package com.hrznstudio.spatial.api;

import improbable.worker.EntityId;
import net.minecraft.entity.Entity;

public interface ISpatialWorld {
    Entity getEntityById(EntityId id);
    void addSpatial(Entity entity);
    void removeSpatial(Entity entity);
}
