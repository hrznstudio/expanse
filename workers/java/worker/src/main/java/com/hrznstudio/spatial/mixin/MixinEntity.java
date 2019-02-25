package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.api.ISpatialEntity;
import com.hrznstudio.spatial.api.ISpatialWorld;
import improbable.worker.EntityId;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity implements ISpatialEntity {
    @Shadow
    public abstract World getEntityWorld();

    private EntityId spatialId;

    @Override
    public EntityId getSpatialId() {
        return spatialId;
    }

    @Override
    public void setSpatialId(EntityId id) {
        ISpatialWorld world = (ISpatialWorld) getEntityWorld();
        world.removeSpatial((Entity) (Object) this);
        spatialId = id;
        world.addSpatial((Entity) (Object) this);
    }
}