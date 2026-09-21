package com.witcherbb.bettersound.music;

import com.mojang.logging.LogUtils;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.bean.Note;
import com.witcherbb.bettersound.music.bean.PianoSongTrack;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSStopPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * 自动演奏引擎：一台钢琴一个实例，NBS 与 MIDI <b>共用</b>。
 *
 * <p>它只认 {@link PianoSongTrack} 这一份最小数据（音符表 + tempo + 小节长度）：
 * <ul>
 *     <li>曲目从哪来、怎么投递 → 由 {@code NBSPlayer} / {@code MidiPlayer} 各自负责；</li>
 *     <li>延音什么时候释放 → 由随曲目一起传入的 {@link SustainPolicy} 决定。</li>
 * </ul>
 * 所以这里是「两者相同的那部分」，NBS 与 MIDI 的差异一点都没有渗进来。
 *
 * <p>客户端播放状态（是否在播、曲目名）也存在这里，因此两种曲目天然互斥，
 * 暂停/继续/停止走的是同一套逻辑与同一批同步数据包。
 */
public class AutoMusicPlayer {
    protected static final short LAST_DELAY = 60; // tick
    private final BlockEntity blockEntity;
    private final Block block;

    /** 播放中每游戏刻自增一次；-1 表示本次播放还没有派发过音符 */
    private int gameTick = -1;
    /** 已经派发过的最后一个歌曲刻；-1 表示还没有派发过 */
    private int songTick = -1;
    /** 停止（或播完）后延迟释放琴键的倒计时；负数表示不在倒计时中 */
    private int stopDelay = -1;
    /** Server side：当前曲目 */
    private PianoSongTrack track;
    /** Server side：延音释放策略，换歌时随曲目一起更新 */
    private SustainPolicy sustainPolicy = SustainPolicy.BAR_LINE;
    private boolean isPlaying;
    private int noteCount;
    /** Client side：本次播放的曲目名 */
    private String clientSongName;

    public AutoMusicPlayer(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.block = blockEntity.getBlockState().getBlock();
    }

    /** Server side */
    public void tick() {
        if (this.isPlaying && this.track != null) {
            this.gameTick++;
            // 先问策略本刻要不要释放延音（可能会切掉正在响的音），再派发本刻到期的音符
            if (this.sustainPolicy.shouldRelease(this.gameTick)) {
                this.stopNote();
            }
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
     * 派发所有「最接近的时刻已经到达」的歌曲刻。
     * <p>
     * tempo 高于 20 刻/秒时一个游戏刻里会有多个歌曲刻到期，低于时会有连续几个游戏刻都没有音符，
     * 所以这里用循环而不是每刻只取一个。判定交给 {@link MusicTiming#toGameTick}：
     * 四舍五入到最近的游戏刻，单个音符误差不超过半个游戏刻（25ms），并且不会随时间累积。
     */
    private void playDueNotes() {
        PianoSongTrack track = this.track;
        while (MusicTiming.toGameTick(this.songTick + 1, track.tempo()) <= this.gameTick) {
            int tick = ++this.songTick;
            List<Note> notes = track.getNotes(tick);
            if (notes == null) continue;
            // 小节线：按歌曲刻判定（而不是用游戏刻去除以速度），速度不是整数时才不会错位。
            // 是否启用由策略决定——MIDI 自带踏板时间轴时会关掉它，改由 shouldRelease 驱动。
            if (this.sustainPolicy.releaseOnBarLine() && tick % track.subsectionLength() == 0) {
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
                // 每个音符都强制延音：踏板抬起之后新弹的音因此依然是饱满的长音，不会变成断奏
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

    /**
     * 客户端开始播放前的公共入口：互斥检查 + 登记曲目名。
     * <p>
     * NBS 与 MIDI 共用同一份客户端播放状态，所以同一台钢琴上两者天然互斥。
     * 调用方负责在返回 {@code true} 之后把各自的播放请求发给服务端。
     *
     * @return 是否真的进入了客户端播放状态；方块不在客户端时返回 false
     */
    public boolean beginClientPlay(String songName) throws PlayerIsPlayingMusicException {
        if (this.isPlaying) throw new PlayerIsPlayingMusicException();
        Level level = this.blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            this.clientSongName = songName;
            this.isPlaying = true;
            return true;
        }
        return false;
    }

    /** Server side：开始（或从暂停中继续）一首曲目 */
    public void play(PianoSongTrack track, SustainPolicy sustainPolicy) {
        if (this.blockEntity.getLevel() == null || this.blockEntity.getLevel().isClientSide) return;
        if (this.isPlaying) return;
        // 只有换歌（或重新开始）才复位进度；同一首歌从暂停中继续时保留播放位置与剩余音符数
        boolean restart = this.track == null || !this.track.name().equals(track.name());
        this.track = track;
        if (restart) {
            this.gameTick = -1;
            this.songTick = -1;
            this.noteCount = track.length();
            this.sustainPolicy = sustainPolicy;
            LogUtils.getLogger().debug("AutoPlay {}: tempo={} ({} 歌曲刻/秒, {} 游戏刻/歌曲刻)",
                    track.name(), track.tempo(),
                    MusicTiming.songTicksPerSecond(track.tempo()),
                    MusicTiming.gameTicksPerSongTick(track.tempo()));
        }
        this.stopDelay = -1;
        this.isPlaying = true;
    }

    /** Both side */
    public void stop() {
        this.isPlaying = false;
        Level level = this.blockEntity.getLevel();
        if (level == null) return;
        if (level.isClientSide) {
            this.clientSongName = null;
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
        return level.isClientSide ? this.clientSongName != null : this.track != null;
    }

    //client
    public String getSongName() {
        return this.clientSongName;
    }
}
