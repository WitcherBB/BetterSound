package com.witcherbb.bettersound.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.witcherbb.bettersound.music.nbs.NBSAutoPlayer;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.nbs.CCommandPlayNBSPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PlayNBSCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(Commands.literal("playnbs").requires(commandSourceStack -> {
            return commandSourceStack.hasPermission(1);
        }).then(Commands.argument("target", BlockPosArgument.blockPos()).then(Commands.literal("play").then(Commands.argument("fileName", StringArgumentType.string()).executes(ctx -> {
            return play(ctx.getSource(), StringArgumentType.getString(ctx, "fileName"), BlockPosArgument.getBlockPos(ctx, "target"));
        })).executes(ctx -> {
            return keepPlaying(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "target"));
        })).then(Commands.literal("stop").executes(ctx -> {
            return stop(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "target"));
        })).then(Commands.literal("pause").executes(ctx -> {
            return pause(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "target"));
        }))));
    }

    private static int play(CommandSourceStack pSource, String fileName, BlockPos traget) {
        ServerPlayer player = pSource.getPlayer();
        if (!(player.level().getBlockEntity(traget) instanceof NBSAutoPlayer)) {
            player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            return -1;
        }
        ModNetwork.sendToPlayer(new CCommandPlayNBSPacket(fileName, traget), player);
        return 0;
    }

    private static int stop(CommandSourceStack pSource, BlockPos target) {
        ServerPlayer player = pSource.getPlayer();
        if (player == null) return -1;
        if (player.level().getBlockEntity(target) instanceof NBSAutoPlayer autoPlayer) {
            autoPlayer.getNBSPlayer().stop();
        } else {
            player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            return -1;
        }

        return 0;
    }

    private static int pause(CommandSourceStack pSource, BlockPos target) {
        ServerPlayer player = pSource.getPlayer();
        if (player == null) return -1;
        if (player.level().getBlockEntity(target) instanceof NBSAutoPlayer autoPlayer) {
            autoPlayer.getNBSPlayer().pause();
        } else {
            player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            return -1;
        }
        return 0;
    }

    private static int keepPlaying(CommandSourceStack pSource, BlockPos target) {
        ServerPlayer player = pSource.getPlayer();
        if (player == null) return -1;
        if (player.level().getBlockEntity(target) instanceof NBSAutoPlayer autoPlayer) {
            autoPlayer.getNBSPlayer().playOn();
        } else {
            player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            return -1;
        }

        return 0;
    }
}
