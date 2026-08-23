package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.nbs.bean.Note;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.music.nbs.bean.PianoSongTrack;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSStopPacket;
import com.witcherbb.bettersound.network.protocol.server.nbs.SNBSPlayPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class NBSPlayer {
    protected static final short LAST_DELAY = 60; // tick
    private final BlockEntity blockEntity;
    private final Block block;

    private int tick = -1;
    /** Client side */
    private PianoSong playingSong;
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
            List<Note> notes = this.track.getNotes(this.tick);
            boolean flag = (this.tick / this.track.speed()) % this.track.subsectionLength() == 0;

            if (notes != null) {
                // play notes
                Note[] noteArray = notes.toArray(Note[]::new);
                if (flag) {
                    this.stopNote();
                }
                this.playNote(noteArray);

                if (--this.noteCount <= 0) {
                    this.stop();
                }
            }
        } else if (!this.isPlaying && this.tick >= 0) {
            Level level = this.blockEntity.getLevel();
            if (--this.tick == -1 && this.block instanceof PianoBlock pianoBlock && level != null) {
                pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), false);
            }
        }
    }

    private void playNote(Note[] notes) {
        if (this.block instanceof PianoBlock pianoBlock) {
            Level level = this.blockEntity.getLevel();
            if (level != null) {
                pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), true);
                pianoBlock.playSounds(null, notes, level, this.blockEntity.getBlockPos());
            }
        }
    }

    private void stopNote() {
        if (this.block instanceof PianoBlock pianoBlock) {
            Level level = this.blockEntity.getLevel();
            if (level != null) {
                pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), false);
            }
        }
    }

    /** Client side */
    @OnlyIn(Dist.CLIENT)
    public void play(PianoSong song) throws PlayerIsPlayingMusicException {
        if (this.isPlaying) throw new PlayerIsPlayingMusicException();
        if (this.blockEntity.getLevel() != null && this.blockEntity.getLevel().isClientSide) {
            this.playingSong = song;
            this.isPlaying = true;
            // 发给服务端数据包
            ModNetwork.sendToServer(new SNBSPlayPacket(this.blockEntity.getBlockPos(), song.fileName, song.getNoteMap(), song.speed, song.timeSignature));
        }
    }

    /** Server side */
    public void play(PianoSongTrack track) {
        if (this.blockEntity.getLevel() != null && !this.blockEntity.getLevel().isClientSide) {
            if (!this.isPlaying) {
                if (this.track == null || !this.track.name().equals(track.name())) this.tick = -1;
                this.track = track;
                this.noteCount = this.track.length();
                this.isPlaying = true;
            }
        }
    }

    /** Both side */
    public void stop() {
        this.isPlaying = false;
        Level level = this.blockEntity.getLevel();
        if (level == null) return;
        if (level.isClientSide) {
            this.playingSong = null;
        } else {
            ModNetwork.broadcast(new CNBSStopPacket(this.blockEntity.getBlockPos()));

            this.track = null;
            this.tick = LAST_DELAY;
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
        return level.isClientSide ? this.playingSong != null : this.track != null;
    }

    //client
    public String getSongName() {
        return this.playingSong.fileName;
    }
}
