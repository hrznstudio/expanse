package com.hrznstudio.spatial.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hrznstudio.spatial.util.Converters;
import improbable.Coordinates;
import improbable.Position;
import improbable.collections.Option;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.View;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import net.minecraft.util.math.BlockPos;

public class ClientView extends View {

    private final ChunkStorageData empty = ChunkStorageData.create();
    private final BiMap<BlockPos, EntityId> posToIdChunks = HashBiMap.create();
    private final BiMap<EntityId, BlockPos> idToPosChunks = posToIdChunks.inverse();

    public ClientView() {
        this.onRemoveEntity(op -> idToPosChunks.remove(op.entityId));

        ClientView.this.onAddComponent(Position.COMPONENT, op -> posToIdChunks.put(Converters.improbableToBlockPos(op.data), op.entityId));
        ClientView.this.onRemoveComponent(Position.COMPONENT, op -> idToPosChunks.remove(op.entityId));
        ClientView.this.onComponentUpdate(Position.COMPONENT, op -> {
            Option<Coordinates> coords = op.update.getCoords();
            if (coords.isPresent()) posToIdChunks.put(Converters.improbableToBlockPos(coords.get()), op.entityId);
        });
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
    public ChunkStorageData getChunkFromBlock(final BlockPos pos) {
        return getChunk(new BlockPos(pos.getX() >> 16, pos.getY() >> 16, pos.getZ() >> 16));
    }
}
