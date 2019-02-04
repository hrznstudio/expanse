package com.hrznstudio.spatial.client.vanillawrappers;

import minecraft.world.ChunkStorageData;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class SpatialChunkProvider extends ChunkProviderClient {

    public SpatialChunkProvider(World worldIn) {
        super(worldIn);
    }

    @Override
    public Chunk loadChunk(int chunkX, int chunkZ) {
        Chunk chunk = new SpatialChunk(this.world, new SpatialPrimer(), chunkX, chunkZ);
        this.loadedChunks.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
        chunk.markLoaded(true);
        return chunk;
    }

    @Override
    public String makeString() {
        return "SpatialChunkProvider: " + this.loadedChunks.size() + ", " + this.loadedChunks.size();
    }

    public void setChunk(final BlockPos chunkPos, final ChunkStorageData chunkStorageData) {
        Chunk chunk = getLoadedChunk(chunkPos.getX(), chunkPos.getZ());
        if (chunk == null) chunk = loadChunk(chunkPos.getX(), chunkPos.getZ());
        final Chunk finalChunk = chunk;
        chunkStorageData.getBlocks().forEach((integer, state) -> finalChunk.setBlockState(new BlockPos(integer >> 8, chunkPos.getY() + (integer >> 4), integer), Block.REGISTRY.getObject(new ResourceLocation(state.getBlock().getId())).getStateFromMeta(state.getMeta())));
        chunk.getHeightMap();
        chunk.generateSkylightMap();
    }
}
