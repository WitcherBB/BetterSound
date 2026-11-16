package com.witcherbb.bettersound.platform.client.forge;

import com.witcherbb.bettersound.platform.client.events.RegisterClientCommandsHandler;
import com.witcherbb.bettersound.forge.platform.EventRegisterApis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.LevelEvent;

import java.util.function.Consumer;

public final class ClientEventRegistersImpl {
    public static void registerRegisterClientCommands(RegisterClientCommandsHandler handler) {
        EventRegisterApis.registerForgeEvent(RegisterCommandsEvent.class, event -> {
            handler.handle(event.getDispatcher(), event.getBuildContext());
        });
    }
}
