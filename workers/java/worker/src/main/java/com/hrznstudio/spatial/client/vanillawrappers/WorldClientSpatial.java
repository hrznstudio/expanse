package com.hrznstudio.spatial.client.vanillawrappers;

import com.hrznstudio.spatial.SpatialMod;
import com.hrznstudio.spatial.client.ClientView;
import com.hrznstudio.spatial.util.Converters;
import mcp.MethodsReturnNonnullByDefault;
import minecraft.world.ChunkStorageData;
import minecraft.world.State;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldClientSpatial extends WorldClient {

    public WorldClientSpatial(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(netHandler, settings, dimension, difficulty, profilerIn);
    }

    @Override
    public IBlockState getBlockState(final BlockPos pos) {
        final ClientView view = SpatialMod.getClientWorker().getView();
        if (view == null) return Blocks.AIR.getDefaultState();
        final ChunkStorageData chunk = view.getChunkFromBlock(pos);
        if (chunk == null) return Blocks.AIR.getDefaultState();
        final State state = chunk.getBlocks().get(Converters.blockPosToChunkIndex(pos));
        if (state == null) return Blocks.AIR.getDefaultState();
        final net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromName(state.getBlock().getId());
        if (block == null) return Blocks.AIR.getDefaultState();
        //noinspection deprecation
        return block.getStateFromMeta(state.getMeta());
    } // In Kotlin, all of those null checks would be chained as one >.<
}
