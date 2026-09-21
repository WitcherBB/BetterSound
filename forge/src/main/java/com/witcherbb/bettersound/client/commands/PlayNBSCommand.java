package com.witcherbb.bettersound.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.exception.NBSNotFoundException;
import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.nbs.SAutoPlayerActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class PlayNBSCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(Commands.literal("playnbs").requires(commandSourceStack -> commandSourceStack.hasPermission(1))
                .then(Commands.argument("position", BlockPosArgument.blockPos())
                        .then(Commands.literal("play")
                                .then(Commands.argument("fileName", StringArgumentType.string())
                                        .suggests(PlayNBSCommand::suggestLocalSongs)
                                        .executes(ctx ->
                                                play(StringArgumentType.getString(ctx, "fileName"), BlockPosArgument.getBlockPos(ctx, "position"))))
                                .executes(ctx ->
                                        sendAction(BlockPosArgument.getBlockPos(ctx, "position"), SAutoPlayerActionPacket.Action.PLAY_ON)))
                        .then(Commands.literal("stop").executes(ctx ->
                                sendAction(BlockPosArgument.getBlockPos(ctx, "position"), SAutoPlayerActionPacket.Action.STOP)))
                        .then(Commands.literal("pause")
                                .executes(ctx ->
                                        sendAction(BlockPosArgument.getBlockPos(ctx, "position"), SAutoPlayerActionPacket.Action.PAUSE))))
                .then(Commands.literal("reload").executes(ctx ->
                        reload())));
    }

    private static CompletableFuture<Suggestions> suggestLocalSongs(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ModSoundManager.INSTANCE.getNbsLoader().getSongs().keySet()
                .parallelStream().map("\"%s\""::formatted), builder);
    }

    private static int play(String fileName, BlockPos target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return -1;
        try {
            BlockPos voicePos = PianoBlock.getVoiceSectionPos(mc.level.getBlockState(target), target, PianoBlock.MIDDEL_PART);
            PianoSong song = ModSoundManager.INSTANCE.getNbsLoader().findSong(fileName);
            BlockEntity entity = mc.level.getBlockEntity(voicePos);
            if (entity instanceof AutoPlayer autoPlayer) {
                autoPlayer.getNBSPlayer().play(song);
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.empty().append(Component.translatable("bettersound.nbs.success", song.fileName)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN));
                }
                return 0;
            }
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            }
        } catch (NBSNotFoundException | PlayerIsPlayingMusicException e) {
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
            }
        }
        return 0;
    }

    private static int sendAction(BlockPos target, SAutoPlayerActionPacket.Action action) {
        ModNetwork.sendToServer(new SAutoPlayerActionPacket(target, action));
        return 0;
    }

    private static int reload() {
        Minecraft mc = Minecraft.getInstance();
        ModSoundManager.INSTANCE.getNbsLoader().load();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("bettersound.nbs.loadscs").withStyle(ChatFormatting.GREEN));
        }
        return 0;
    }
}
