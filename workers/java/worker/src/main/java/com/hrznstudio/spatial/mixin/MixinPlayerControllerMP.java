package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.SpatialMod;
import com.hrznstudio.spatial.api.ISpatialEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {
    @Shadow
    @Final
    private NetHandlerPlayClient connection;

    @Shadow
    @Final
    private Minecraft mc;

    /**
     * @author Coded
     */
    @Overwrite
    public EntityPlayerSP createPlayer(World p_192830_1_, StatisticsManager p_192830_2_, RecipeBook p_192830_3_) {
        EntityPlayerSP sp = new EntityPlayerSP(this.mc, p_192830_1_, this.connection, p_192830_2_, p_192830_3_);
        ((ISpatialEntity) sp).setSpatialId(SpatialMod.getClientWorker().getPlayerId());
        return sp;
    }
}