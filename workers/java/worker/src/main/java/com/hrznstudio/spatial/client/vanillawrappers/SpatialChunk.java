package com.hrznstudio.spatial.client.vanillawrappers;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

public class SpatialChunk extends Chunk {
    public SpatialChunk(World worldIn, ChunkPrimer primer, int x, int z) {
        super(worldIn, primer, x, z);
    }
}
