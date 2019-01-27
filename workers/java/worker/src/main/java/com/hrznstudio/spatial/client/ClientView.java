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
        super();

        //TODO: this all needs to detect whether the entitiy is actually a chunk
        this.onRemoveEntity(op -> idToPosChunks.remove(op.entityId));
        this.onAddComponent(Position.COMPONENT, op -> posToIdChunks.put(Converters.improbableToBlockPos(op.data), op.entityId));
    }

    public void removeChunk(EntityId chunk) {
        if (idToPosChunks.containsKey(chunk)) {
            posToIdChunks.remove(idToPosChunks.get(chunk));
            idToPosChunks.remove(chunk);
        }
    }

    public void addChunk(BlockPos pos, EntityId chunk) {
        idToPosChunks.put(chunk, pos);
        posToIdChunks.put(pos, chunk);
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
