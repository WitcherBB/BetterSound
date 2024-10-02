package com.witcherbb.bettersound.mixins.mixins;

import com.mojang.datafixers.DataFixer;
import com.witcherbb.bettersound.common.data.ModDataManager;
import com.witcherbb.bettersound.mixins.extenders.MinecraftServerExtender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtender {

    @Unique
    private ModDataManager betterSound$modDataManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void MinecraftServer0(Thread pServerThread, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, Proxy pProxy, DataFixer pFixerUpper, Services pServices, ChunkProgressListenerFactory pProgressListenerFactory, CallbackInfo ci) {
        this.betterSound$modDataManager = new ModDataManager();
    }

    @Override
    public ModDataManager betterSound$getModDataManager() {
        return this.betterSound$modDataManager;
    }
}
