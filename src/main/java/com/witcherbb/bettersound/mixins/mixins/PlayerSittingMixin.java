package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.blocks.entity.utils.Sittable;
import com.witcherbb.bettersound.mixins.extenders.CouldSittingExtender;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSittingMixin extends LivingEntity implements CouldSittingExtender {
    @Shadow protected abstract boolean wantsToStopRiding();

    @Shadow public abstract void rideTick();

    @Shadow public abstract void remove(RemovalReason pReason);

    @Shadow public float oBob;
    @Shadow public float bob;
    @Unique
    protected Player betterSound$self = (Player) (Object) this;
    @Unique
    protected Sittable<?> betterSound$seat;

    protected PlayerSittingMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    public void travel0(Vec3 pTravelVector, CallbackInfo ci) {
        if (this.betterSound$isSitting()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick0(CallbackInfo ci) {
        if (this.betterSound$isSitting() && (this.wantsToStopRiding() || this.betterSound$seat.isRemoved())) {
            this.betterSound$stopSitting();
            this.setShiftKeyDown(false);
        }
        if (this.betterSound$isSitting()) {
            this.setOldPosAndRot();
            this.tickCount++;
            this.betterSound$getSeat().positionPassenger(this.betterSound$self);
            this.oRun = this.run;
            this.run = 0.0F;
            this.resetFallDistance();
            this.oBob = this.bob;
            this.bob = 0.0F;
        }
    }

    @Override
    public Sittable<?> betterSound$getSeat() {
        return this.betterSound$seat;
    }

    @Override
    public boolean betterSound$isSitting() {
        return this.betterSound$seat != null;
    }

    @Override
    public boolean isPassenger() {
        return super.isPassenger();
    }

    @Override
    public void betterSound$stopSitting() {
        if (!this.level().isClientSide) {
            this.betterSound$dismountSeat(this.betterSound$seat);
        }
        this.betterSound$seat.removePassenger(betterSound$self);
        this.betterSound$seat = null;
    }

    @Override
    public boolean betterSound$startSitting(Sittable<?> pSeat) {
        if (this.betterSound$seat == pSeat) {
            return false;
        } else {
            if (this.betterSound$canSit(pSeat) && pSeat.canAddPassenger(betterSound$self)) {
                if (this.betterSound$isSitting()) {
                    this.betterSound$stopSitting();
                }
                if (this.isPassenger()) {
                    this.stopRiding();
                }

                this.setPose(Pose.STANDING);
                this.betterSound$seat = pSeat;
                this.betterSound$seat.addPassenger(betterSound$self);
                return true;
            }
        }
        return false;
    }

    @Unique
    protected boolean betterSound$canSit(Sittable<?> seat) {
        return !this.isShiftKeyDown();
    }

    @Override
    public void betterSound$dismountSeat(Sittable<?> seat) {
        Vec3 vec3;
        if (this.isRemoved()) {
            vec3 = this.position();
        } else if (!seat.isRemoved()) {
            vec3 = seat.getDismountLocationForPassenger(betterSound$self);
        } else {
            vec3 = new Vec3(this.getX(), this.getY(), this.getZ());
        }

        this.setPos(vec3.x, vec3.y, vec3.z);
    }
}
