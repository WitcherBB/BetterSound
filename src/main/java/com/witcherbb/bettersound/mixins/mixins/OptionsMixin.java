package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.client.ModOptions;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "save", at = @At("RETURN"))
    public void save0(CallbackInfo ci) {
        ModOptions.getOptions().save();
    }
}
