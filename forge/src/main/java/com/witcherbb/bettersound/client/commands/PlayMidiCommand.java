package com.witcherbb.bettersound.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.exception.MidiNotFoundException;
import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.midi.bean.MidiSong;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
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

/**
 * {@code /playmidi}：与 {@link PlayNBSCommand} 同构，只是曲库换成 {@code midi_bettersound} 目录。
 * <p>播放、暂停、继续、停止都复用 {@link SAutoPlayerActionPacket} 与 NBS 那套客户端同步数据包，
 * 因为 MIDI 与 NBS 共用同一个播放器实例。
 */
@OnlyIn(Dist.CLIENT)
public class PlayMidiCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(Commands.literal("playmidi").requires(commandSourceStack -> commandSourceStack.hasPermission(1))
                .then(Commands.argument("position", BlockPosArgument.blockPos())
                        .then(Commands.literal("play")
                                .then(Commands.argument("fileName", StringArgumentType.string())
                                        .suggests(PlayMidiCommand::suggestLocalSongs)
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
        return SharedSuggestionProvider.suggest(ModSoundManager.INSTANCE.getMidiLoader().getSongs().keySet()
                .parallelStream().map("\"%s\""::formatted), builder);
    }

    private static int play(String fileName, BlockPos target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return -1;
        try {
            BlockPos voicePos = PianoBlock.getVoiceSectionPos(mc.level.getBlockState(target), target, PianoBlock.MIDDEL_PART);
            MidiSong song = ModSoundManager.INSTANCE.getMidiLoader().findSong(fileName);
            BlockEntity entity = mc.level.getBlockEntity(voicePos);
            if (entity instanceof AutoPlayer autoPlayer && autoPlayer.getMidiPlayer() != null) {
                autoPlayer.getMidiPlayer().play(song);
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.empty().append(Component.translatable("bettersound.midi.success", song.name)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN));
                }
                return 0;
            }
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            }
        } catch (MidiNotFoundException | PlayerIsPlayingMusicException e) {
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
        ModSoundManager.INSTANCE.getMidiLoader().load();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("bettersound.midi.loadscs").withStyle(ChatFormatting.GREEN));
        }
        return 0;
    }
}
