package com.witcherbb.bettersound.client.network;

import com.witcherbb.bettersound.blocks.AbstractPianoBlock;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.client.gui.screen.inventory.BetterJukeboxScreen;
import com.witcherbb.bettersound.client.gui.screen.inventory.PianoBlockScreen;
import com.witcherbb.bettersound.client.gui.screen.inventory.ToneBlockScreen;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.exception.NBSNotFoundException;
import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.AutoMusicPlayer;
import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.network.protocol.client.CBetterJukeboxNameConfirmPacket;
import com.witcherbb.bettersound.network.protocol.client.COpenPianoScreenPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CCommandPlayNBSPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSReloadPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSStopPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoPlayMultipleNotesPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoPlaySingleNotePacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoStopNotesExceptPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoStopSingleNotePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class ClientPacketHandlers {

    public static void betterJukeboxNameConfirm(CBetterJukeboxNameConfirmPacket packet) {
        if (Minecraft.getInstance().screen instanceof BetterJukeboxScreen screen) {
            screen.getImageTip().updateStatus(packet.status());
        }
    }

    public static void openPianoScreen(COpenPianoScreenPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockPos pos = packet.pos();
        BlockState blockState = mc.level.getBlockState(pos);
        if (blockState.is(ModBlocks.PIANO_BLOCK.get())) {
            mc.setScreen(new PianoBlockScreen(pos));
        } else if (blockState.is(ModBlocks.TONE_BLOCK.get())) {
            mc.setScreen(new ToneBlockScreen(pos));
        }
    }

    public static void commandPlayNBS(CCommandPlayNBSPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        try {
            BlockPos target = PianoBlock.getSectionPos(mc.level.getBlockState(packet.pos()), packet.pos(), PianoBlock.MIDDEL_PART);
            PianoSong song = ModSoundManager.INSTANCE.getNbsLoader().findSong(packet.filename());
            BlockEntity entity = mc.level.getBlockEntity(target);
            if (entity instanceof AutoPlayer autoPlayer) {
                autoPlayer.getNBSPlayer().play(song);
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.empty()
                            .append(Component.translatable("bettersound.nbs.success", song.fileName))
                            .withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN));
                }
            }
        } catch (NBSNotFoundException | PlayerIsPlayingMusicException e) {
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
            }
        }
    }

    public static void nbsPause(CNBSPausePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos());
        if (!(blockEntity instanceof AutoPlayer autoPlayer)) {
            return;
        }
        AutoMusicPlayer musicPlayer = autoPlayer.getMusicPlayer();
        if (!musicPlayer.hasSong() && mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.hasnosong").withStyle(ChatFormatting.RED));
        } else if (!musicPlayer.isPlaying() && mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotplaying", musicPlayer.getSongName()).withStyle(ChatFormatting.RED));
        } else {
            musicPlayer.pause();
        }
    }

    public static void nbsPlayOn(CNBSPlayOnPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos());
        if (!(blockEntity instanceof AutoPlayer autoPlayer)) {
            return;
        }
        AutoMusicPlayer musicPlayer = autoPlayer.getMusicPlayer();
        if (!musicPlayer.hasSong() && mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.hasnosong").withStyle(ChatFormatting.RED));
        } else if (musicPlayer.isPlaying() && mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("bettersound.nbs.success", musicPlayer.getSongName()).withStyle(ChatFormatting.RED));
        } else {
            musicPlayer.playOn();
        }
    }

    public static void nbsReload(CNBSReloadPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        ModSoundManager.INSTANCE.getNbsLoader().load();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("bettersound.nbs.loadscs").withStyle(ChatFormatting.GREEN));
        }
    }

    public static void nbsStop(CNBSStopPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos());
        if (!(blockEntity instanceof AutoPlayer autoPlayer)) {
            return;
        }
        // 播放状态只有一份（在共用引擎里），所以 NBS 与 MIDI 用的是同一套同步包
        AutoMusicPlayer musicPlayer = autoPlayer.getMusicPlayer();
        if (!musicPlayer.hasSong() && mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.hasnosong").withStyle(ChatFormatting.RED));
        } else {
            musicPlayer.stop();
        }
    }

    public static void pianoPlayMultipleNotes(CPianoPlayMultipleNotesPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Note[] notes = packet.notes();
        Vec3[] positions = packet.positions();
        if (positions.length != notes.length) {
            return;
        }
        ModSoundManager soundManager = ModSoundManager.INSTANCE;
        BlockEntity blockEntity = mc.level.getBlockEntity(packet.sourcePos());
        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            // 过滤无效音调（旧版解析器残留的 -1 哨兵等），防止 pianoSounds.get() 越界崩溃
            if (note.getPitch() < 0 || note.getPitch() >= ModSoundEvents.pianoSounds.size()) {
                continue;
            }
            if (blockEntity instanceof PianoBlockEntity) {
                soundManager.playPianoSound(ModSoundEvents.pianoSounds.get(note.getPitch()).get(), packet.playerUUID(),
                        packet.sourcePos(), positions[i], note.getPitch(), Note.toPianoSoundVolume(note.getVolume()), false, false);
            }
        }
    }

    public static void pianoPlaySingleNote(CPianoPlaySingleNotePacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockState blockState = level.getBlockState(packet.blockPos());
        if (blockState.getBlock() instanceof AbstractPianoBlock) {
            ModSoundManager.INSTANCE.playPianoSound(ModSoundEvents.pianoSounds.get(packet.tone()).get(), packet.playerUUID(),
                    packet.blockPos(), packet.voicePos(), packet.tone(), Note.toPianoSoundVolume(packet.volume()),
                    packet.isForUI(), packet.isShort());
        }
    }

    public static void pianoStopNotesExcept(CPianoStopNotesExceptPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        ModSoundManager.INSTANCE.tryToStopAllPianoSounds(packet.pos(), packet.tones());
    }

    public static void pianoStopSingleNote(CPianoStopSingleNotePacket packet) {
        ModSoundManager.INSTANCE.tryToStopPianoSound(packet.playerUUID(), packet.playingPos(), packet.tone());
    }

    private ClientPacketHandlers() {
    }
}
