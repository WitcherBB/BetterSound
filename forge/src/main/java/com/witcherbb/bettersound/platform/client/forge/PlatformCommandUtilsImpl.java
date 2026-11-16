package com.witcherbb.bettersound.platform.client.forge;

import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class PlatformCommandUtilsImpl {
    public static BlockPos getBlockPos(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> commandContext, String name) {
        return BlockPosArgument.getBlockPos((CommandContext<CommandSourceStack>) (CommandContext<?>) commandContext, name);
    }
}
