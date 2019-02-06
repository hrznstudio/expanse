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
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.NibbleArrayReader;

public class ClientView extends View {

    private final ChunkStorageData empty = ChunkStorageData.create();
    private final BiMap<BlockPos, EntityId> posToIdChunks = HashBiMap.create();
    private final BiMap<EntityId, BlockPos> idToPosChunks = posToIdChunks.inverse();

    public ClientView() {
        this.onRemoveEntity(op -> removeChunk(op.entityId));
        new ComponentRequirement(this, id -> {
            Entity entity = ClientView.this.entities.get(id);
            final Option<PositionData> pos = entity.get(Position.COMPONENT);
            if (!pos.isPresent())
                return;
            final BlockPos chunkPos = Converters.improbableToChunkPos(pos.get());
            final Option<MetadataData> meta = entity.get(Metadata.COMPONENT);
            if (meta.isPresent() && meta.get().getEntityType().equals(ChunkWorker.CHUNK))
                addChunk(chunkPos, id);
            final Option<ChunkStorageData> storage = entity.get(ChunkStorage.COMPONENT);
            if (storage.isPresent())
                ((WorldClientSpatial) Minecraft.getMinecraft().world).loadChunk(chunkPos, storage.get());
        }, Position.COMPONENT, Metadata.COMPONENT, ChunkStorage.COMPONENT);
        this.onComponentUpdate(ChunkStorage.COMPONENT, argument -> {
            EntityId id = argument.entityId;
            Entity entity = ClientView.this.entities.get(id);
            final Option<PositionData> pos = entity.get(Position.COMPONENT);
            if (!pos.isPresent())
                return;
            final BlockPos chunkPos = Converters.improbableToChunkPos(pos.get());
            final Option<MetadataData> meta = entity.get(Metadata.COMPONENT);
            if (meta.isPresent() && meta.get().getEntityType().equals(ChunkWorker.CHUNK)) {
                //TODO: Change blocks based on update
            }
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
