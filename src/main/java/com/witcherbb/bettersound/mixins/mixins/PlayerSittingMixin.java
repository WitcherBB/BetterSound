package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.blocks.entity.utils.Sittable;
import com.witcherbb.bettersound.mixins.extenders.PlayerSittingExtender;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSittingMixin extends LivingEntity implements PlayerSittingExtender {
    @Shadow protected abstract boolean wantsToStopRiding();

    @Shadow public abstract void rideTick();

    @Shadow public abstract void remove(RemovalReason pReason);

    @Shadow public float oBob;
    @Shadow public float bob;
    @Unique
    protected Player self = (Player) (Object) this;
    @Unique
    protected Sittable<?> seat;

    protected PlayerSittingMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    public void travel0(Vec3 pTravelVector, CallbackInfo ci) {
        if (this.isSitting()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick0(CallbackInfo ci) {
        if (this.isSitting() && (this.wantsToStopRiding() || this.seat.isRemoved())) {
            this.stopSitting();
            this.setShiftKeyDown(false);
        }
        if (this.isSitting()) {
            this.setOldPosAndRot();
            this.tickCount++;
            this.getSeat().positionPassenger(this.self);
            this.oRun = this.run;
            this.run = 0.0F;
            this.resetFallDistance();
            this.oBob = this.bob;
            this.bob = 0.0F;
        }
    }

    @Override
    public Sittable<?> getSeat() {
        return this.seat;
    }

    @Override
    public boolean isSitting() {
        return this.seat != null;
    }

    @Override
    public boolean isPassenger() {
        return super.isPassenger();
    }

    @Override
    public void stopSitting() {
        if (!this.level().isClientSide) {
            this.dismountVehicle(this.seat);
        }
        this.seat.removePassenger(self);
        this.seat = null;
//        this.setNoGravity(false);
    }

    @Override
    public boolean startSitting(Sittable<?> pSeat) {
        if (this.seat == pSeat) {
            return false;
        } else {
            if (this.canSit(pSeat) && pSeat.canAddPassenger(self)) {
                if (this.isSitting()) {
                    this.stopSitting();
                }

                this.setPose(Pose.STANDING);
//                this.setNoGravity(true);
                this.seat = pSeat;
                this.seat.addPassenger(self);
                return true;
            }
        }
        return false;
    }

    @Unique
    protected boolean canSit(Sittable<?> seat) {
        return !this.isShiftKeyDown();
    }

    @Override
    public void dismountVehicle(Sittable<?> seat) {
        Vec3 vec3;
        if (this.isRemoved()) {
            vec3 = this.position();
        } else if (!seat.isRemoved()) {
            vec3 = seat.getDismountLocationForPassenger(self);
        } else {
            vec3 = new Vec3(this.getX(), this.getY(), this.getZ());
        }

        this.dismountTo(vec3.x, vec3.y, vec3.z);
    }
}
