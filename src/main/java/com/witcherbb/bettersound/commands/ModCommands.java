package com.witcherbb.bettersound.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.witcherbb.bettersound.server.commands.PlayNBSCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandBuildContext context) {
        PlayNBSCommand.register(dispatcher, context);
    }
}
