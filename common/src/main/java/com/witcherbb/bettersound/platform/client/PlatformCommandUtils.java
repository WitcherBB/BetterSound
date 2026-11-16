package com.witcherbb.bettersound.platform.client;

import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public final class PlatformCommandUtils {
    @ExpectPlatform
    public static BlockPos getBlockPos(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> commandContext, String name) {
        throw new AssertionError();
    }
}
