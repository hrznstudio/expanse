package com.hrznstudio.spatial.client.vanillawrappers;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

@MethodsReturnNonnullByDefault
public class WorldClientSpatial extends WorldClient {

    public WorldClientSpatial(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(netHandler, settings, dimension, difficulty, profilerIn);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return super.getBlockState(pos);
    }
}
