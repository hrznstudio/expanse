package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.SpatialMod;
import com.hrznstudio.spatial.util.ConnectionManager;
import improbable.Coordinates;
import improbable.Position;
import improbable.worker.EntityId;
import minecraft.entity.Motion;
import minecraft.entity.Player;
import minecraft.entity.Rotation;
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
        if (ConnectionManager.getConnectionStatus().isConnected()) {
            boolean flag = playerSP.isSprinting();

            EntityId id = SpatialMod.getClientWorker().getPlayerId();
            if (id == null) {
                return;
            }

            boolean uSp = false, uSn = false;

            if (flag != serverSprintState) {
                uSp = true;
                serverSprintState = flag;
            }

            boolean flag2 = playerSP.isSneaking();

            if (flag2 != serverSneakState) {
                uSn = true;
                serverSneakState = flag2;
            }
            if (uSn || uSp) {
                ConnectionManager.getConnection().sendComponentUpdate(Player.COMPONENT, id, new Player.Update().setSneaking(flag2).setSprinting(flag));
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
                ConnectionManager.getConnection().sendComponentUpdate(Position.COMPONENT, id, new Position.Update().setCoords(new Coordinates(playerSP.posX, playerSP.posY, playerSP.posZ)));
                this.lastReportedPosX = playerSP.posX;
                this.lastReportedPosY = axisalignedbb.minY;
                this.lastReportedPosZ = playerSP.posZ;
                this.positionUpdateTicks = 0;
            }

            if (rot) {
                ConnectionManager.getConnection().sendComponentUpdate(Rotation.COMPONENT, id, new Rotation.Update().setPitch(playerSP.rotationPitch).setYaw(playerSP.rotationYaw));
                this.lastReportedYaw = playerSP.rotationYaw;
                this.lastReportedPitch = playerSP.rotationPitch;
            }
            ConnectionManager.getConnection().sendComponentUpdate(Motion.COMPONENT, id, new Motion.Update().setCoords(new Coordinates(
                    playerSP.motionX,
                    playerSP.motionY,
                    playerSP.motionZ
            )));

            this.positionUpdateTicks = 0;
        }
    }
}