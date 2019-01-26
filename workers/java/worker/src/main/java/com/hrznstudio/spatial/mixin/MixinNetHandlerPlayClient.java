package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.client.vanillawrappers.WorldClientSpatial;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Redirect(method = "handleJoinGame", at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/WorldClient"))
    public WorldClient newWorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        return new WorldClientSpatial(netHandler, settings, dimension, difficulty, profilerIn);
    }
}
