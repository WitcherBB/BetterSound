package com.witcherbb.bettersound.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class CommandUtils {
    static final LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> CLIENT_COMMAND_ROOT_BUILDER =
            ClientCommandRegistrationEvent.literal("bettersound");
}
