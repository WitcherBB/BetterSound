package com.witcherbb.bettersound.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.exception.MidiNotFoundException;
import com.witcherbb.bettersound.exception.NBSNotFoundException;
import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.midi.bean.MidiSong;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.nbs.SAutoPlayerActionPacket;
import com.witcherbb.bettersound.platform.client.PlatformCommandUtils;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class AutoPlayCommand {
    private static final Function<PlayThroughFilenameWorker, RequiredArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack, String>> PLAY_THROUGH_FILENAME_BUILDER =
            worker -> ClientCommandRegistrationEvent.argument("fileName", StringArgumentType.string())
                    .suggests(worker::suggestLocalSongs)
                    .executes(ctx ->
                            worker.play(StringArgumentType.getString(ctx, "fileName"),
                                    PlatformCommandUtils.getBlockPos(ctx, "position")));

    private static final Supplier<LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack>> PLAY_MIDI_BUILDER =
            () -> ClientCommandRegistrationEvent.literal("midi")
                    .then(PLAY_THROUGH_FILENAME_BUILDER.apply(new PlayThroughFilenameWorker() {
                        @Override
                        public int play(String fileName, BlockPos target) {
                            return playMidi(fileName, target);
                        }

                        @Override
                        public CompletableFuture<Suggestions> suggestLocalSongs(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> ctx, SuggestionsBuilder builder) {
                            return SharedSuggestionProvider.suggest(ModSoundManager.INSTANCE.getMidiLoader().getSongs().keySet()
                                    .parallelStream().map("\"%s\""::formatted), builder);
                        }
                    }));

    private static final Supplier<LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack>> PLAY_NBS_BUILDER =
            () -> ClientCommandRegistrationEvent.literal("nbs")
                    .then(PLAY_THROUGH_FILENAME_BUILDER.apply(new PlayThroughFilenameWorker() {
                        @Override
                        public int play(String fileName, BlockPos target) {
                            return playNbs(fileName, target);
                        }

                        @Override
                        public CompletableFuture<Suggestions> suggestLocalSongs(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> ctx, SuggestionsBuilder builder) {
                            return SharedSuggestionProvider.suggest(ModSoundManager.INSTANCE.getNbsLoader().getSongs().keySet()
                                    .parallelStream().map("\"%s\""::formatted), builder);
                        }
                    }));

    public static void register(CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(CommandUtils.CLIENT_COMMAND_ROOT_BUILDER.requires(commandSourceStack -> commandSourceStack.hasPermission(1))
                .then(ClientCommandRegistrationEvent.argument("position", BlockPosArgument.blockPos())
                        .then(ClientCommandRegistrationEvent.literal("play")
                                .then(PLAY_MIDI_BUILDER.get())
                                .then(PLAY_NBS_BUILDER.get())
                                .executes(ctx ->
                                        sendAction(PlatformCommandUtils.getBlockPos(ctx, "position"), SAutoPlayerActionPacket.Action.PLAY_ON)))
                        .then(ClientCommandRegistrationEvent.literal("stop").executes(ctx ->
                                sendAction(PlatformCommandUtils.getBlockPos(ctx, "position"), SAutoPlayerActionPacket.Action.STOP)))
                        .then(ClientCommandRegistrationEvent.literal("pause")
                                .executes(ctx ->
                                        sendAction(PlatformCommandUtils.getBlockPos(ctx, "position"), SAutoPlayerActionPacket.Action.PAUSE))))
                .then(ClientCommandRegistrationEvent.literal("reload").executes(ctx ->
                        reload())));
    }

    private static CompletableFuture<Suggestions> suggestLocalSongs(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ModSoundManager.INSTANCE.getMidiLoader().getSongs().keySet()
                .parallelStream().map("\"%s\""::formatted), builder);
    }

    private static int playMidi(String fileName, BlockPos target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return -1;
        try {
            BlockState targetState = mc.level.getBlockState(target);
            if (targetState.is(ModBlocks.PIANO_BLOCK.get())) {
                BlockPos mainPos = PianoBlock.getSectionPos(targetState, target, PianoBlock.MIDDEL_PART);
                MidiSong song = ModSoundManager.INSTANCE.getMidiLoader().findSong(fileName);
                AutoPlayer autoPlayer = (AutoPlayer) mc.level.getBlockEntity(mainPos);
                Objects.requireNonNull(autoPlayer);
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

    private static int playNbs(String fileName, BlockPos target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return -1;
        try {
            BlockState targetState = mc.level.getBlockState(target);
            if (targetState.is(ModBlocks.PIANO_BLOCK.get())) {
                BlockPos mainPos = PianoBlock.getSectionPos(targetState, target, PianoBlock.MIDDEL_PART);
                PianoSong song = ModSoundManager.INSTANCE.getNbsLoader().findSong(fileName);
                AutoPlayer autoPlayer = (AutoPlayer) mc.level.getBlockEntity(mainPos);
                Objects.requireNonNull(autoPlayer);
                autoPlayer.getNBSPlayer().play(song);
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.empty().append(Component.translatable("bettersound.midi.success", song.name)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN));
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
        ModSoundManager.INSTANCE.getMidiLoader().load();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("bettersound.midi.loadscs").withStyle(ChatFormatting.GREEN));
        }
        return 0;
    }

    private interface PlayThroughFilenameWorker {
        int play(String fileName, BlockPos target);

        CompletableFuture<Suggestions> suggestLocalSongs(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> ctx, SuggestionsBuilder builder);
    }
}
