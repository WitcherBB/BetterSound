package com.witcherbb.bettersound.platform.client;

import com.witcherbb.bettersound.platform.client.events.RegisterClientCommandsHandler;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.LevelAccessor;

import java.util.function.Consumer;

public final class ClientEventRegisters {
    /**
     * 注册"命令注册"事件的 handler
     */
    @ExpectPlatform
    public static void registerRegisterClientCommands(RegisterClientCommandsHandler handler) {
        throw new AssertionError();
    }
}
