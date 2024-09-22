package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExtender {

    @Unique
    private ModSoundManager betterSound$modSoundManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void Minecraft0(GameConfig pGameConfig, CallbackInfo ci) {
        this.betterSound$modSoundManager = new ModSoundManager((Minecraft) (Object) this);
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;destroy()V"))
    private void close0(CallbackInfo ci) {
        this.betterSound$modSoundManager.destroy();
    }

    @Inject(method = "updateScreenAndTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop()V"))
    private void updateScreenAndTick0(Screen pScreen, CallbackInfo ci) {
        this.betterSound$modSoundManager.stopAll();
    }

    @Override
    public ModSoundManager getmodSoundManager() {
        return this.betterSound$modSoundManager;
    }
}
