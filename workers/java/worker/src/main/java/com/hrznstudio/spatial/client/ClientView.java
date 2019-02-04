package com.hrznstudio.spatial.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hrznstudio.spatial.client.vanillawrappers.WorldClientSpatial;
import com.hrznstudio.spatial.util.Converters;
import com.hrznstudio.spatial.worker.chunk.ChunkWorker;
import improbable.Metadata;
import improbable.MetadataData;
import improbable.Position;
import improbable.PositionData;
import improbable.collections.Option;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.View;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class ClientView extends View {

    private final ChunkStorageData empty = ChunkStorageData.create();
    private final BiMap<BlockPos, EntityId> posToIdChunks = HashBiMap.create();
    private final BiMap<EntityId, BlockPos> idToPosChunks = posToIdChunks.inverse();

    public ClientView() {
        this.onRemoveEntity(op -> removeChunk(op.entityId));
        // TODO: abstract away some system to require several components and listen to all of them
        // TODO: listen to chunk content updates
        this.onAddComponent(Position.COMPONENT, op -> {
            final BlockPos chunkPos = Converters.improbableToChunkPos(op.data);
            final Option<MetadataData> meta = ClientView.this.entities.get(op.entityId).get(Metadata.COMPONENT);
            if (meta.isPresent() && meta.get().getEntityType().equals(ChunkWorker.CHUNK))
                addChunk(chunkPos, op.entityId);
            final Option<ChunkStorageData> storage = ClientView.this.entities.get(op.entityId).get(ChunkStorage.COMPONENT);
            if (storage.isPresent())
                ((WorldClientSpatial) Minecraft.getMinecraft().world).loadChunk(chunkPos, storage.get());
        });
        this.onAddComponent(Metadata.COMPONENT, op -> {
            if (op.data.getEntityType().equals(ChunkWorker.CHUNK)) {
                final Option<PositionData> pos = ClientView.this.entities.get(op.entityId).get(Position.COMPONENT);
                if (pos.isPresent()) addChunk(Converters.improbableToChunkPos(pos.get()), op.entityId);
            }
        });
        this.onAddComponent(ChunkStorage.COMPONENT, op -> {
            final Option<PositionData> pos = ClientView.this.entities.get(op.entityId).get(Position.COMPONENT);
            if (pos.isPresent())
                ((WorldClientSpatial) Minecraft.getMinecraft().world).loadChunk(Converters.improbableToChunkPos(pos.get()), op.data);
        });
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
