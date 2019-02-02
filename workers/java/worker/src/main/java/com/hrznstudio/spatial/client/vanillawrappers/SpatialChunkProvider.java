package com.hrznstudio.spatial.client.vanillawrappers;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import minecraft.world.ChunkStorageData;
import minecraft.world.State;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class SpatialChunkProvider extends ChunkProviderClient {

    private final Long2ObjectMap<SpatialChunk> loadedChunks = new Long2ObjectOpenHashMap<SpatialChunk>(8192) {
        protected void rehash(int p_rehash_1_) {
            if (p_rehash_1_ > this.key.length) {
                super.rehash(p_rehash_1_);
            }
        }
    };

    public SpatialChunkProvider(World worldIn) {
        super(worldIn);
    }

    @Nullable
    @Override
    public SpatialChunk getLoadedChunk(int x, int z) {
        return loadedChunks.get(ChunkPos.asLong(x, z));
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        return MoreObjects.firstNonNull(getLoadedChunk(x, z), blankChunk);
    }

    @Override
    public boolean tick() {
        return super.tick();
    }

    @Override
    @Nonnull
    public String makeString() {
        return "SpatialChunkProvider: " + this.loadedChunks.size() + ", " + this.loadedChunks.size();
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return loadedChunks.containsValue(ChunkPos.asLong(x, z));
    }

    public void setChunk(BlockPos pos, ChunkStorageData chunkStorageData) {

        SpatialChunk chunk = getLoadedChunk(pos.getX(), pos.getZ());
        if (chunk == null) {
            chunk = new SpatialChunk(world, new SpatialPrimer(), pos.getX(), pos.getZ());
            loadedChunks.put(ChunkPos.asLong(chunk.x, chunk.z), chunk);
        }
        SpatialChunk finalChunk = chunk;
        chunkStorageData.getBlocks().forEach(new BiConsumer<Integer, State>() {
            @Override
            public void accept(Integer integer, State state) {
                finalChunk.setBlockState(new BlockPos((integer >> 8) % 16, pos.getY() + (integer >> 4) % 16, integer % 16), Block.REGISTRY.getObject(new ResourceLocation(state.getBlock().getId())).getStateFromMeta(state.getMeta()));
            }
        });
        chunk.getHeightMap();
        chunk.generateSkylightMap();
    }
}
