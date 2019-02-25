package com.hrznstudio.spatial.client.vanillawrappers;

import com.hrznstudio.spatial.SpatialMod;
import com.hrznstudio.spatial.client.ClientView;
import com.hrznstudio.spatial.util.Converters;
import minecraft.world.ChunkStorageData;
import minecraft.world.State;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

public class SpatialPrimer extends ChunkPrimer {
    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        final ClientView view = SpatialMod.getClientWorker().getDispatcher();
        if (view == null) return Blocks.AIR.getDefaultState();
        final ChunkStorageData chunk = view.getChunkFromBlock(x, y, z);
        if (chunk == null) return Blocks.AIR.getDefaultState();
        final State state = chunk.getBlocks().get(Converters.blockPosToChunkIndex(x, y, z));
        if (state == null) return Blocks.AIR.getDefaultState();
        final net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromName(state.getBlock().getId());
        if (block == null) return Blocks.AIR.getDefaultState();
        //noinspection deprecation
        return block.getStateFromMeta(state.getMeta());
    }
}