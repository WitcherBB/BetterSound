package com.witcherbb.bettersound.forge.platform;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

public final class EventRegisterApis {
    public static <T extends Event> void registerForgeEvent(Class<T> ignored, Consumer<T> consumer) {
        MinecraftForge.EVENT_BUS.addListener(consumer);
    }

    public static <T extends Event> void registerModEvent(Class<T> ignored, Consumer<T> consumer) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(consumer);
    }
}
