package com.witcherbb.bettersound.mixins.mixins;

import com.mojang.authlib.GameProfile;
import com.witcherbb.bettersound.mixins.extenders.LocalPlayerExtender;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements LocalPlayerExtender {

    @Shadow @Final public ClientPacketListener connection;
    @Shadow public Input input;
    @Shadow @Final private List<AmbientSoundHandler> ambientSoundHandlers;

    @Shadow protected abstract boolean vehicleCanSprint(Entity pVehicle);

    @Shadow protected abstract boolean hasEnoughImpulseToStartSprinting();

    @Shadow protected abstract boolean hasEnoughFoodToStartSprinting();

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER), cancellable = true)
    public void tick0(CallbackInfo ci) {
        if (this.betterSound$isSitting()) {
            this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround()));
            this.connection.send(new ServerboundPlayerInputPacket(this.xxa, this.zza, this.input.jumping, this.input.shiftKeyDown));
            for(AmbientSoundHandler ambientsoundhandler : this.ambientSoundHandlers) {
                ambientsoundhandler.tick();
            }
            ci.cancel();
        }
    }

    @Inject(method = "canStartSprinting", at = @At("RETURN"), cancellable = true)
    public void canStartSprinting0(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
                !this.isSprinting() && this.hasEnoughImpulseToStartSprinting() && this.hasEnoughFoodToStartSprinting() && !this.isUsingItem() && !this.hasEffect(MobEffects.BLINDNESS) && (!this.isPassenger() || this.vehicleCanSprint(this.getVehicle())) && !this.isFallFlying()
                && !this.betterSound$isSitting()
        );
    }
}
