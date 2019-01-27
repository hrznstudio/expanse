package com.hrznstudio.spatial.client.vanillawrappers;

import com.hrznstudio.spatial.SpatialMod;
import com.hrznstudio.spatial.util.Converters;
import improbable.Position;
import improbable.PositionData;
import improbable.collections.Option;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import mcp.MethodsReturnNonnullByDefault;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldClientSpatial extends WorldClient {

    public WorldClientSpatial(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(netHandler, settings, dimension, difficulty, profilerIn);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        this.clientChunkProvider = new SpatialChunkProvider(this);
        return this.clientChunkProvider;
    }

    @Override
    public void tick() {

    }

    public void refreshChunks() {
        SpatialMod.getClientWorker().getDispatcher().entities.forEach(new BiConsumer<EntityId, Entity>() {
            @Override
            public void accept(EntityId entityId, Entity entity) {
                Option<ChunkStorageData> dataOption = entity.get(ChunkStorage.COMPONENT);
                Option<PositionData> positionOption = entity.get(Position.COMPONENT);
                if(dataOption.isPresent()&&positionOption.isPresent()) {
                    ((SpatialChunkProvider)chunkProvider).setChunk(Converters.improbableToBlockPos(positionOption.get()), dataOption.get());
                }
            }
        });
    }
}
