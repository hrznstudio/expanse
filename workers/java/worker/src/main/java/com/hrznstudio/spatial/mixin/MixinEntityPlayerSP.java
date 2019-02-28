package com.hrznstudio.spatial.mixin;

import com.hrznstudio.spatial.api.ISpatialEntity;
import com.hrznstudio.spatial.util.ConnectionManager;
import improbable.Vector3f;
import improbable.worker.EntityId;
import minecraft.entity.Rotation;
import minecraft.player.PlayerInput;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

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
            EntityId id = ((ISpatialEntity) playerSP).getSpatialId();
            if (id == null) {
                return;
            }

            PlayerInput.Update update = new PlayerInput.Update();

            boolean sprinting = playerSP.isSprinting();
            boolean sneaking = playerSP.isSneaking();

            if (sprinting != serverSprintState) {
                update.setSprinting(sprinting);
                serverSprintState = sprinting;
            }

            if (sneaking != serverSneakState) {
                update.setSneaking(sneaking);
                serverSneakState = sneaking;
            }

            ++this.positionUpdateTicks;
            AxisAlignedBB axisalignedbb = playerSP.getEntityBoundingBox();
            double xChange = playerSP.posX - this.lastReportedPosX;
            double yChange = axisalignedbb.minY - this.lastReportedPosY;
            double zChange = playerSP.posZ - this.lastReportedPosZ;
            boolean pos = xChange * xChange + yChange * yChange + zChange * zChange > 9.0E-4D;
            boolean rot = (playerSP.rotationYaw - this.lastReportedYaw) != 0.0D || (playerSP.rotationPitch - this.lastReportedPitch) != 0.0D;

            if (pos) {
                update.setMovePosition(new Vector3f((float) playerSP.posX, (float) playerSP.posY, (float) playerSP.posZ));
                this.lastReportedPosX = playerSP.posX;
                this.lastReportedPosY = axisalignedbb.minY;
                this.lastReportedPosZ = playerSP.posZ;
                this.positionUpdateTicks = 0;
            }
            update.setDesiredMotion(new Vector3f((float) playerSP.motionX, (float) playerSP.motionY, (float) playerSP.motionZ));

            if (rot) {
                ConnectionManager.getConnection().sendComponentUpdate(Rotation.COMPONENT, id, new Rotation.Update().setPitch(playerSP.rotationPitch).setYaw(playerSP.rotationYaw));
                this.lastReportedYaw = playerSP.rotationYaw;
                this.lastReportedPitch = playerSP.rotationPitch;
            }

            ConnectionManager.getConnection().sendComponentUpdate(PlayerInput.COMPONENT, id, update);

            this.positionUpdateTicks = 0;
        }
    }
}