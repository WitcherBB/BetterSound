package com.witcherbb.bettersound.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.witcherbb.bettersound.client.commands.AutoPlayCommand;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.commands.CommandBuildContext;

public class Commands {
    public static void registerOnClient(CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher, CommandBuildContext context) {
        AutoPlayCommand.register(dispatcher, context);
    }
}
