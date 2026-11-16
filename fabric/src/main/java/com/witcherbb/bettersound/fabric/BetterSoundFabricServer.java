package com.witcherbb.bettersound.fabric;

import com.witcherbb.bettersound.fabric.config.FabricCommonConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

@Environment(EnvType.SERVER)
public class BetterSoundFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        FabricCommonConfig.HANDLER.load();
        registerEvents();
    }

    private static void registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register(EventHandlers::onServerStopped);
    }

    private static final class EventHandlers {
        static void onServerStopped(MinecraftServer server) {
            FabricCommonConfig.HANDLER.save();
        }
    }
}
