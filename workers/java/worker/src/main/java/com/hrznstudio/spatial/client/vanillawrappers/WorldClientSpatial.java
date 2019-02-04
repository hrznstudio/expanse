package com.hrznstudio.spatial.client.vanillawrappers;

import minecraft.world.ChunkStorageData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;

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

    public void loadChunk(final BlockPos chunkPos, final ChunkStorageData chunkData) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ((SpatialChunkProvider) chunkProvider).setChunk(chunkPos, chunkData);
            markBlockRangeForRenderUpdate(chunkPos.getX() << 4, chunkPos.getY() << 4, chunkPos.getZ() << 4, (chunkPos.getX() << 4) + 15, (chunkPos.getY() << 4) + 15, (chunkPos.getZ() << 4) + 15);
        });
    }

    public void unloadChunk(final BlockPos chunkPos) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ((SpatialChunkProvider) chunkProvider).unloadChunk(chunkPos.getX(), chunkPos.getZ());
            markBlockRangeForRenderUpdate(chunkPos.getX() * 16, chunkPos.getY() << 4, chunkPos.getZ() << 4, (chunkPos.getX() << 4) + 15, (chunkPos.getY() << 4) + 15, chunkPos.getZ() * 16 + 15);
        });
    }
}
