package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.music.nbs.bean.Note;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.music.nbs.bean.PianoSongTrack;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.nbs.CNBSStopPacket;
import com.witcherbb.bettersound.network.protocol.CPianoBlockPlayMultipleNotesPacket;
import com.witcherbb.bettersound.network.protocol.nbs.SNBSPlayPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class NBSPlayer {
    private final BlockEntity blockEntity;
    private final Block block;

    private int tick = -1;
    /** Client side */
    @OnlyIn(Dist.CLIENT)
    private PianoSong playing;
    /** Server side */
    private PianoSongTrack track;
    private boolean isPlaying;
    private int noteCount;

    public NBSPlayer(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.block = blockEntity.getBlockState().getBlock();
    }

    /** Server side */
    public void tick() {
        if (this.isPlaying && this.track != null) {
            this.tick++;
            List<Note> notes;
            if ((notes = this.track.getNotes(this.tick)) != null) {
                // play notes
                if (this.tick % this.track.getSubsectionLength() == 0) {

                    ModNetwork.broadcast(new CPianoBlockPlayMultipleNotesPacket(null, this.blockEntity.getBlockPos(), new byte[0], new byte[0], true));
                }
                ModNetwork.broadcast(new CPianoBlockPlayMultipleNotesPacket(null, this.blockEntity.getBlockPos(), Note.getTones(notes), Note.getVolumes(notes), false));

                if (--this.noteCount <= 0) {
                    this.stop();
                }
            }
        }
    }

    private void playNote() {
        if (this.block instanceof PianoBlock pianoBlock) {

        }
    }

    private void stopNote() {
        if (this.block instanceof PianoBlock pianoBlock) {

        }
    }

    /** Client side */
    @OnlyIn(Dist.CLIENT)
    public void play(PianoSong song) {
        if (this.blockEntity.getLevel() != null && this.blockEntity.getLevel().isClientSide && !this.isPlaying) {
            this.playing = song;
            this.isPlaying = true;
            // 发给服务端数据包
            ModNetwork.sendToServer(new SNBSPlayPacket(this.blockEntity.getBlockPos(), song.getNoteMap(), song.speed, song.timeSignature));
        }
    }

    /** Server side */
    public void play(PianoSongTrack track) {
        if (this.blockEntity.getLevel() != null && !this.blockEntity.getLevel().isClientSide) {
            if (!this.isPlaying) {
                this.track = track;
                this.noteCount = this.track.length();
                this.isPlaying = true;
            }
        }
    }

    /** Both side */
    public void stop() {
        Level level = this.blockEntity.getLevel();
        if (level == null) return;
        this.isPlaying = false;
        if (level.isClientSide) {
            this.playing = null;
        } else {
            ModNetwork.broadcast(new CNBSStopPacket(this.blockEntity.getBlockPos()));
            this.track = null;
            this.tick = -1;
        }
    }

    /** Both side */
    public void pause() {
        Level level = this.blockEntity.getLevel();
        if (level == null) return;
        this.isPlaying = false;
        if (!level.isClientSide) {
            ModNetwork.broadcast(new CNBSPausePacket(this.blockEntity.getBlockPos()));
        }
    }

    /** Both side */
    public void playOn() {
        Level level = this.blockEntity.getLevel();
        if (level == null) return;
        this.isPlaying = true;
        if (!level.isClientSide) {
            ModNetwork.broadcast(new CNBSPlayOnPacket(this.blockEntity.getBlockPos()));
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean hasSong() {
        Level level = this.blockEntity.getLevel();
        if (level == null) return false;
        return level.isClientSide ? this.playing != null : this.track != null;
    }

    @OnlyIn(Dist.CLIENT)
    public String getSongName() {
        return this.playing.fileName;
    }
}
