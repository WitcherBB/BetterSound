package com.witcherbb.bettersound.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.witcherbb.bettersound.client.commands.PlayMidiCommand;
import com.witcherbb.bettersound.client.commands.PlayNBSCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class ModCommands {
    
    public static void registerOnClient(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        PlayNBSCommand.register(dispatcher, context);
        PlayMidiCommand.register(dispatcher, context);
    }
}
