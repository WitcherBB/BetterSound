package com.witcherbb.bettersound.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.witcherbb.bettersound.client.commands.PlayNBSCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModCommands {
    @OnlyIn(Dist.CLIENT)
    public static void registerOnClient(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        PlayNBSCommand.register(dispatcher, context);
    }
}
