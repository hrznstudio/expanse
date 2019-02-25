package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.api.ISpatialEntity;
import com.hrznstudio.spatial.api.ISpatialWorld;
import improbable.worker.EntityId;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld implements ISpatialWorld {
    private final Long2ObjectMap<Entity> entitiesBySpatialId = new Long2ObjectOpenHashMap<>();


    @Inject(method = "onEntityAdded", at = @At("HEAD"))
    public void onEntityAdded(Entity entity, CallbackInfo info) {
        addSpatial(entity);
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    public void onEntityRemoved(Entity entity, CallbackInfo info) {
        removeSpatial(entity);
    }

    @Override
    public Entity getEntityById(EntityId id) {
        return entitiesBySpatialId.get(id.getInternalId());
    }

    @Override
    public void addSpatial(Entity entity) {
        if (entity instanceof ISpatialEntity && ((ISpatialEntity) entity).getSpatialId() != null)
            entitiesBySpatialId.put(((ISpatialEntity) entity).getSpatialId().getInternalId(), entity);
    }

    @Override
    public void removeSpatial(Entity entity) {
        if (entity instanceof ISpatialEntity && ((ISpatialEntity) entity).getSpatialId() != null)
            entitiesBySpatialId.remove(((ISpatialEntity) entity).getSpatialId().getInternalId(), entity);
    }
}