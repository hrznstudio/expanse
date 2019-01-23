package com.hrznstudio.spatial.client;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

public class WorldClientSpatial extends WorldClient {
    public WorldClientSpatial(NetHandlerPlayClient netHandler, WorldSettings settings, Profiler profilerIn) {
        super(netHandler, settings, 0, EnumDifficulty.PEACEFUL, profilerIn);
    }

}
