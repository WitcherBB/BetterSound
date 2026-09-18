package com.witcherbb.bettersound.music.nbs;

import com.mojang.logging.LogUtils;
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

    /** 播放中每游戏刻自增一次；-1 表示本次播放还没有派发过音符 */
    private int gameTick = -1;
    /** 已经派发过的最后一个 NBS 歌曲刻；-1 表示还没有派发过 */
    private int songTick = -1;
    /** 停止（或播完）后延迟释放琴键的倒计时；负数表示不在倒计时中 */
    private int stopDelay = -1;
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
            this.gameTick++;
            this.playDueNotes();
        } else if (this.stopDelay >= 0) {
            // 停止后延迟若干刻再解除延音，让已经发声的音符自然衰减
            Level level = this.blockEntity.getLevel();
            if (--this.stopDelay < 0 && this.block instanceof PianoBlock pianoBlock && level != null) {
                pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), false);
            }
        }
    }

    /**
     * 派发所有「最接近的时刻已经到达」的 NBS 歌曲刻。
     * <p>
     * tempo 高于 20 刻/秒时一个游戏刻里会有多个歌曲刻到期，低于时会有连续几个游戏刻都没有音符，
     * 所以这里用循环而不是每刻只取一个。判定交给 {@link NbsTiming#toGameTick}：
     * 四舍五入到最近的游戏刻，单个音符误差不超过半个游戏刻（25ms），并且不会随时间累积。
     */
    private void playDueNotes() {
        PianoSongTrack track = this.track;
        while (NbsTiming.toGameTick(this.songTick + 1, track.tempo()) <= this.gameTick) {
            int tick = ++this.songTick;
            List<Note> notes = track.getNotes(tick);
            if (notes == null) continue;
            // 小节线：按歌曲刻判定（而不是用游戏刻去除以速度），速度不是整数时才不会错位
            if (tick % track.subsectionLength() == 0) {
                this.stopNote();
            }
            this.playNote(notes.toArray(Note[]::new));

            if (--this.noteCount <= 0) {
                this.stop();
                return;
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
            ModNetwork.sendToServer(new SNBSPlayPacket(this.blockEntity.getBlockPos(), song.fileName, song.getNoteMap(), song.tempo, song.timeSignature));
        }
    }

    /** Server side */
    public void play(PianoSongTrack track) {
        if (this.blockEntity.getLevel() != null && !this.blockEntity.getLevel().isClientSide) {
            if (!this.isPlaying) {
                // 只有换歌（或重新开始）才复位进度；同一首歌从暂停中继续时保留播放位置与剩余音符数
                boolean restart = this.track == null || !this.track.name().equals(track.name());
                this.track = track;
                if (restart) {
                    this.gameTick = -1;
                    this.songTick = -1;
                    this.noteCount = track.length();
                    LogUtils.getLogger().debug("NBS {}: tempo={} ({} 歌曲刻/秒, {} 游戏刻/歌曲刻)",
                            track.name(), track.tempo(),
                            NbsTiming.songTicksPerSecond(track.tempo()),
                            NbsTiming.gameTicksPerSongTick(track.tempo()));
                }
                this.stopDelay = -1;
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
            this.stopDelay = LAST_DELAY;
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
