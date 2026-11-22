package com.witcherbb.bettersound.mixins.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.witcherbb.bettersound.client.resources.sounds.SoundInstanceCallback;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "tickNonPaused()V",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Iterator;remove()V",
                    ordinal = 1)
    )
    private void tickNonePaused0(CallbackInfo ci, @Local(ordinal = 0) SoundInstance soundInstance) {
        if (soundInstance instanceof SoundInstanceCallback callback) {
            callback.onStop();
        }
    }
}
