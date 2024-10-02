package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.mixins.extenders.SoundInstanceExtender;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "tickNonPaused()V",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Iterator;remove()V",
                    ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void tickNonePaused0(CallbackInfo ci, Iterator iterator, Map.Entry entry, ChannelAccess.ChannelHandle channelaccess$channelhandle1, SoundInstance soundinstance) {
        ((SoundInstanceExtender) soundinstance).betterSound$onStop();
    }
}
