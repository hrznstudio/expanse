package com.hrznstudio.spatial.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hrznstudio.spatial.client.vanillawrappers.WorldClientSpatial;
import com.hrznstudio.spatial.util.Converters;
import com.hrznstudio.spatial.util.EntityRequirementCallback;
import com.hrznstudio.spatial.util.Util;
import com.hrznstudio.spatial.worker.chunk.ChunkWorker;
import improbable.Position;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.View;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;

public class ClientView extends View {

    private final ChunkStorageData empty = ChunkStorageData.create();
    private final BiMap<BlockPos, EntityId> posToIdChunks = HashBiMap.create();
    private final BiMap<EntityId, BlockPos> idToPosChunks = posToIdChunks.inverse();

    public ClientView() {
        this.onRemoveEntity(op -> removeChunk(op.entityId));
        EntityRequirementCallback.builder(
                (id) -> {
                    Entity entity = getEntity(id);
                    BlockPos pos = Converters.improbableToChunkPos(Util.getData(entity, Position.COMPONENT));
                    addChunk(pos, id);
                    ((WorldClientSpatial) Minecraft.getMinecraft().world).loadChunk(pos, Util.getData(entity, ChunkStorage.COMPONENT));
                })
                .requireType(ChunkWorker.CHUNK)
                .requireComponent(Position.COMPONENT)
                .requireComponent(ChunkStorage.COMPONENT)
                .attach(this);
    }

    @Nullable
    public Entity getEntity(EntityId id) {
        return entities.get(id);
    }

    private void removeChunk(EntityId chunk) {
        BlockPos pos = idToPosChunks.remove(chunk);
        if (pos != null) ((WorldClientSpatial) Minecraft.getMinecraft().world).unloadChunk(pos);
    }

    private void addChunk(BlockPos pos, EntityId chunk) {
        posToIdChunks.put(pos, chunk);
    }

    /**
     * @param pos chunk position, in chunk coordinates
     * @return chunk at given position
     */
    public ChunkStorageData getChunk(final BlockPos pos) {
        EntityId id = posToIdChunks.get(pos);
        if (id == null) return empty;
        Entity entity = getEntity(id);
        if (entity == null) return empty;
        return entity.get(ChunkStorage.COMPONENT).orElse(empty);
    }

    /**
     * @param pos chunk position, in block coordinates
     * @return chunk at given position
     */
    public ChunkStorageData getChunkFromBlock(final Vec3i pos) {
        return getChunkFromBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public ChunkStorageData getChunkFromBlock(final int x, final int y, final int z) {
        return getChunk(new BlockPos(x >> 4, y >> 4, z >> 4));
    }
}
