package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.SpatialMod;
import improbable.Coordinates;
import improbable.Position;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import minecraft.entity.Player;
import minecraft.entity.Rotation;
import minecraft.world.ChunkStorage;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Shadow
    private int positionUpdateTicks;

    @Shadow
    private boolean serverSprintState;

    @Shadow
    private boolean serverSneakState;

    @Shadow
    private double lastReportedPosX;

    @Shadow
    private double lastReportedPosY;

    @Shadow
    private double lastReportedPosZ;

    @Shadow
    private float lastReportedYaw;

    @Shadow
    private float lastReportedPitch;

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V"))
    public void onUpdateWalkingPlayer(EntityPlayerSP playerSP) {
        if (SpatialMod.isConnectedToSpatial()) {
            boolean flag = playerSP.isSprinting();

            if(SpatialMod.getPlayerId()==null) {
                return;
            }

            if (flag != serverSprintState) {
//                player.get(Player.COMPONENT).get().setSprinting(flag);
                serverSprintState = flag;
            }

            boolean flag2 = playerSP.isSneaking();

            if (flag2 != serverSneakState) {
//                player.get(Player.COMPONENT).get().setSneaking(flag);
                serverSneakState = flag2;
            }

            ++this.positionUpdateTicks;
            AxisAlignedBB axisalignedbb = playerSP.getEntityBoundingBox();
            double d0 = playerSP.posX - this.lastReportedPosX;
            double d1 = axisalignedbb.minY - this.lastReportedPosY;
            double d2 = playerSP.posZ - this.lastReportedPosZ;
            double d3 = (double) (playerSP.rotationYaw - this.lastReportedYaw);
            double d4 = (double) (playerSP.rotationPitch - this.lastReportedPitch);
            boolean pos = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D;
            boolean rot = d3 != 0.0D || d4 != 0.0D;

            if (pos) {
                SpatialMod.connection.sendComponentUpdate(Position.COMPONENT, SpatialMod.getPlayerId(), new Position.Update().setCoords(new Coordinates(playerSP.posX, playerSP.posY, playerSP.posZ)));
                this.lastReportedPosX = playerSP.posX;
                this.lastReportedPosY = axisalignedbb.minY;
                this.lastReportedPosZ = playerSP.posZ;
                this.positionUpdateTicks = 0;
            }

            if (rot) {
//                player.update(Rotation.COMPONENT, new Rotation.Update().setPitch(playerSP.rotationPitch).setYaw(playerSP.rotationYaw));
                this.lastReportedYaw = playerSP.rotationYaw;
                this.lastReportedPitch = playerSP.rotationPitch;
            }
            this.positionUpdateTicks = 0;
        }
    }
}