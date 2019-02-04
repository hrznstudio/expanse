package com.hrznstudio.spatial.client.vanillawrappers;

import minecraft.world.ChunkStorageData;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class SpatialChunkProvider extends ChunkProviderClient {

    public SpatialChunkProvider(World worldIn) {
        super(worldIn);
    }

    @Override
    public String makeString() {
        return "SpatialChunkProvider: " + this.loadedChunks.size() + ", " + this.loadedChunks.size();
    }

    public void setChunk(final BlockPos chunkPos, final ChunkStorageData chunkStorageData) {
        Chunk chunk = getLoadedChunk(chunkPos.getX(), chunkPos.getZ());
        if (chunk == null) chunk = loadChunk(chunkPos.getX(), chunkPos.getZ());
        final Chunk finalChunk = chunk;
        //noinspection deprecation
        chunkStorageData.getBlocks().forEach((integer, state) -> finalChunk.setBlockState(
                new BlockPos((integer >> 8) % 16, chunkPos.getY() + (integer >> 4) % 16, integer % 16),
                Block.REGISTRY.getObject(new ResourceLocation(state.getBlock().getId())).getStateFromMeta(state.getMeta()))
        );
        chunk.getHeightMap();
        chunk.generateSkylightMap();
    }
}
