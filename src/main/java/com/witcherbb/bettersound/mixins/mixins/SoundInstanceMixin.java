package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.mixins.extenders.SoundInstanceExtender;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoundInstance.class)
public interface SoundInstanceMixin extends SoundInstanceExtender {
    @Override
    default void betterSound$onStop() {
    }
}
