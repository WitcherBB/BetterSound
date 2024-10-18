package com.witcherbb.bettersound.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.client.nbs.CCommandPlayNBSPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSReloadPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class PlayNBSCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(Commands.literal("playnbs").requires(commandSourceStack -> {
            return commandSourceStack.hasPermission(1);
        }).then(Commands.argument("position", BlockPosArgument.blockPos()).then(Commands.literal("play").then(Commands.argument("fileName", StringArgumentType.string()).executes(ctx -> {
            return play(ctx.getSource(), StringArgumentType.getString(ctx, "fileName"), BlockPosArgument.getBlockPos(ctx, "position"));
        })).executes(ctx -> {
            return playerAct(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "position"), AutoPlayer::playNBSOn);
        })).then(Commands.literal("stop").executes(ctx -> {
            return playerAct(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "position"), AutoPlayer::stopNBS);
        })).then(Commands.literal("pause").executes(ctx -> {
            return playerAct(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "position"), AutoPlayer::pauseNBS);
        }))).then(Commands.literal("reload").executes(ctx -> {
            return reload(ctx.getSource());
        })));
    }

    private static int play(CommandSourceStack pSource, String fileName, BlockPos target) {
        ServerPlayer player = pSource.getPlayer();
        if (player == null) return -1;
        if (!(player.level().getBlockEntity(target) instanceof AutoPlayer)) {
            player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            return -1;
        }
        ModNetwork.sendToPlayer(new CCommandPlayNBSPacket(fileName, target), player);

        return 0;
    }

    private static int playerAct(CommandSourceStack pSource, BlockPos target, Consumer<AutoPlayer> action) {
        BlockPos playerPos = target;
        ServerPlayer player = pSource.getPlayer();
        if (player == null) return -1;
        Level level = player.level();
        BlockState state = level.getBlockState(target);
        if (state.getBlock() instanceof PianoBlock) {
            playerPos = PianoBlock.getVoiceSectionPos(level.getBlockState(target), target, PianoBlock.MIDDEL_PART);
        }

        if (level.getBlockEntity(playerPos) instanceof AutoPlayer autoPlayer) {
            action.accept(autoPlayer);
        } else {
            player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            return -1;
        }

        return 0;
    }

    private static int reload(CommandSourceStack pSource) {
        ServerPlayer player = pSource.getPlayer();
        if (player == null) return -1;
        ModNetwork.sendToPlayer(new CNBSReloadPacket(), player);
        return 0;
    }
}
