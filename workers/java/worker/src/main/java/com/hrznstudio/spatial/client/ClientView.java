package com.hrznstudio.spatial.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hrznstudio.spatial.util.Converters;
import improbable.Position;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.View;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class ClientView extends View {

    private final ChunkStorageData empty = ChunkStorageData.create();
    private final BiMap<BlockPos, EntityId> posToIdChunks = HashBiMap.create();
    private final BiMap<EntityId, BlockPos> idToPosChunks = posToIdChunks.inverse();

    public ClientView() {
        //TODO: this all needs to detect whether the entity is actually a chunk
        this.onRemoveEntity(op -> removeChunk(op.entityId));
        this.onAddComponent(Position.COMPONENT, op -> addChunk(Converters.improbableToBlockPos(op.data), op.entityId));
    }

    public void removeChunk(EntityId chunk) {
        idToPosChunks.remove(chunk);
        //TODO: update render cache
    }

    public void addChunk(BlockPos pos, EntityId chunk) {
        posToIdChunks.put(pos, chunk);
        //TODO: update render cache
    }

    /**
     * @param pos chunk position, in chunk coordinates
     * @return chunk at given position
     */
    public ChunkStorageData getChunk(final BlockPos pos) {
        EntityId id = posToIdChunks.get(pos);
        if (id == null) return empty;
        Entity entity = entities.get(id);
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
