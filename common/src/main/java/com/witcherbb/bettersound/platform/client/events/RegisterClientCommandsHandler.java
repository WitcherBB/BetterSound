package com.witcherbb.bettersound.platform.client.events;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public interface RegisterClientCommandsHandler {
    void handle(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context);
}
