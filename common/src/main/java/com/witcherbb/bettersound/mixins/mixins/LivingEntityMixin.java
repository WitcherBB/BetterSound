package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.entities.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "dismountVehicle(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "RETURN")
    )
    public void bettersound$dismountVehicle0(Entity entity, CallbackInfo ci) {
        if (entity.getType() == ModEntities.FAKE_STOOL_ENTITY.get()) {
            Vec3 passengerPosition = entity.getDismountLocationForPassenger((LivingEntity) (Object) this);
            ((LivingEntity) (Object) this).dismountTo(passengerPosition.x, passengerPosition.y, passengerPosition.z);
        }
    }
}
