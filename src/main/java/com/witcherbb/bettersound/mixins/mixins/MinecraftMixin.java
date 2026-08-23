package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void Minecraft0(GameConfig pGameConfig, CallbackInfo ci) {
//        this.betterSound$modSoundManager = new ModSoundManager((Minecraft) (Object) this);
//        this.betterSound$nbsLoader = new NBSLoader(this.gameDirectory, "nbs_bettersound");
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;destroy()V"))
    private void close0(CallbackInfo ci) {
        ModSoundManager.INSTANCE.destroy();
    }

    @Inject(method = "updateScreenAndTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop()V"))
    private void updateScreenAndTick0(Screen pScreen, CallbackInfo ci) {
        ModSoundManager.INSTANCE.stopAll();
    }
}
