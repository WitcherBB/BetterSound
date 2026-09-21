package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.mixins.extenders.CouldSittingExtender;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Unique
    private final Entity betterSound$instance = (Entity) (Object) this;

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPose(Lnet/minecraft/world/entity/Pose;)V"))
    private void startRiding0(Entity pVehicle, boolean pForce, CallbackInfoReturnable<Boolean> cir) {
        if (betterSound$instance instanceof CouldSittingExtender extender && extender.betterSound$isSitting()) {
            extender.betterSound$stopSitting();
        }
    }
}
