package com.witcherbb.bettersound.fabric;

import com.witcherbb.bettersound.BetterSoundClient;
import com.witcherbb.bettersound.fabric.config.FabricClientConfig;
import com.witcherbb.bettersound.fabric.config.FabricCommonConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class BetterSoundFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientConfig.HANDLER.load();
        FabricCommonConfig.HANDLER.load();
        registerEvents();
        BetterSoundClient.init();
        BetterSoundClient.initPostRegistration();
        BetterSoundClient.registerParticleProviders();
    }

    private static void registerEvents() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(EventHandlers::onClientStopping);
    }

    private static final class EventHandlers {
        static void onClientStopping(Minecraft client) {
        }
    }
}
