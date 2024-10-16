package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.blocks.PianoBlock;
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
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

public class NBSPlayer {
    protected static final short LAST_DELAY = 3000;
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
                Note[] noteArray = notes.toArray(Note[]::new);
                if (this.tick % this.track.getSubsectionLength() == 0) {
                    this.stopNote(noteArray);
                }
                this.playNote(noteArray);

                if (--this.noteCount <= 0) {
                    this.stop();
                }
            }
        } else if (!this.isPlaying) {

        }
    }

    private void playNote(Note[] notes) {
        if (this.block instanceof PianoBlock pianoBlock) {
            for (int i = 0; i < notes.length; i++) {
                Note note = notes[i];
                Level level = this.blockEntity.getLevel();
                if (level != null) {
                    pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), true);
                    pianoBlock.playSound(null, note.getPitch(), note.getVolume(), level, this.blockEntity.getBlockPos());
                }
            }
        }
    }

    private void stopNote(Note[] notes) {
        if (this.block instanceof PianoBlock pianoBlock) {
            for (int i = 0; i < notes.length; i++) {
                Note note = notes[i];
                Level level = this.blockEntity.getLevel();
                if (level != null) {
                    pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), false);
                    pianoBlock.stopSound(null, note.getPitch(), level, this.blockEntity.getBlockPos());
                }
            }
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

            new Thread(() -> {
                try{
                    Thread.sleep(3000);
                    if (this.block instanceof PianoBlock pianoBlock) {
                        ServerLifecycleHooks.getCurrentServer().submit(() -> {
                            pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), false);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

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
