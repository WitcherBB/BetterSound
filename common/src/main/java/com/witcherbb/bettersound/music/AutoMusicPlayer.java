package com.witcherbb.bettersound.music;

import com.mojang.logging.LogUtils;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.common.config.Configs;
import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSStopPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动演奏引擎：一台钢琴一个实例，NBS 与 MIDI <b>共用</b>。
 *
 * <p>它只做一件事：把 {@link PianoSongTrack} 里的事件表按刻派发出去。
 * <ul>
 *     <li>曲目从哪来、怎么投递 → 由 {@code NBSPlayer} / {@code MidiPlayer} 各自负责；</li>
 *     <li>延音什么时候释放 → 已经作为哨兵事件躺在事件表里（{@link Note#KIND_RELEASE} 等），
 *         或者是引擎在每根小节线上的兜底（{@link PianoSongTrack#releaseOnBarLine()}）。</li>
 * </ul>
 * 因为"何时释放"是数据而不是状态，引擎不需要待释放表、也不需要每游戏刻扫描一遍，
 * NBS 与 MIDI 的差异一点都没有渗进来。
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
     * tempo 高于 20 刻/秒时一个游戏刻里会有多个歌曲刻到期，低于时会有连续几个游戏刻都没有事件，
     * 所以这里用循环而不是每刻只取一个。判定交给 {@link MusicTiming#toGameTick}：
     * 四舍五入到最近的游戏刻，单个音符误差不超过半个游戏刻（25ms），并且不会随时间累积。
     */
    private void playDueNotes() {
        PianoSongTrack track = this.track;
        while (MusicTiming.toGameTick(this.songTick + 1, track.tempo()) <= this.gameTick) {
            int tick = ++this.songTick;
            List<Note> events = track.getNotes(tick);
            if (events == null) continue;
            // 小节线兜底：只有曲子里没有自带释放事件的（NBS）才走这条路
            if (track.releaseOnBarLine() && tick % Math.round(track.subsectionLength() * Configs.releaseSubsections()) == 0) {
                this.stopNote();
            }
            this.dispatch(events);

            if (--this.noteCount <= 0) {
                this.stop();
                return;
            }
        }
    }

    /**
     * 派发一刻上的全部事件。
     * <p>
     * <b>释放先于演奏</b>：同一刻既抬踏板又起新音时，要先把旧音收掉、新音才是干净的起振。
     * 这与引入哨兵之前的顺序一致（当时是每刻先 {@code applyPedal} 再派发音符）。
     * <p>
     * 这个保证来自"演奏事件先攒起来、等整刻走完再统一播"，与释放事件在列表里的位置无关；
     * 编译期另外还会把释放插在该刻最前面，把这层意思也写进数据里，两边互相印证。
     * <p>
     * 只有正常音符会被交给 {@link PianoBlock#playSounds}，释放/踏板事件在这里就被消化掉了——
     * 客户端的声音路径永远看不到哨兵。
     */
    private void dispatch(List<Note> events) {
        List<Note> toPlay = null;
        for (Note event : events) {
            if (event.isReleaseAll()) {
                this.stopNote();
            } else if (event.isReleaseTone()) {
                this.releaseNote(event.getPitch());
            } else if (event.isPedal()) {
                this.setPedal(event.isPedalDown());
            } else {
                if (toPlay == null) toPlay = new ArrayList<>(events.size());
                toPlay.add(event);
            }
        }
        if (toPlay != null) {
            this.playNote(toPlay.toArray(Note[]::new));
        }
    }

    private void playNote(Note[] notes) {
        if (this.block instanceof PianoBlock pianoBlock) {
            Level level = this.blockEntity.getLevel();
            if (level != null) {
                // 每个音符都强制延音：释放之后新弹的音因此依然是饱满的长音，不会变成断奏
//                pianoBlock.setDelay(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), true);
                pianoBlock.playSounds(null, notes, level, this.blockEntity.getBlockPos());
            }
        }
    }

    /**
     * 整片收掉（安全阀触发、NBS 的小节线兜底），顺带把踏板外观抬起来。
     * <p>
     * 注意这里<b>不走 {@code setDelay(false)}</b>：那会连带把 {@code isSoundDelay} 置成 false，
     * 而客户端那份状态慢一拍，下一拍的音就会被当成"短音"播。自动演奏期间它应该一直是 true。
     */
    private void stopNote() {
        if (this.block instanceof PianoBlock pianoBlock) {
            Level level = this.blockEntity.getLevel();
            if (level != null) {
                BlockState state = this.blockEntity.getBlockState();
                BlockPos pos = this.blockEntity.getBlockPos();
                pianoBlock.cutSounding(state, level, pos);
                pianoBlock.setPedalState(state, level, pos, false);
            }
        }
    }

    /**
     * 把曲子里的踏板状态画到方块上（模型会跟着变）。
     * <p>注意它<b>不收任何音</b>：该收的音由各自的逐音释放事件负责，这里再顺一刀会把键还按着的音也砍掉。
     */
    private void setPedal(boolean down) {
        if (this.block instanceof PianoBlock pianoBlock) {
            Level level = this.blockEntity.getLevel();
            if (level != null) {
                pianoBlock.setPedalState(this.blockEntity.getBlockState(), level, this.blockEntity.getBlockPos(), down);
            }
        }
    }

    /**
     * 松开某一个键。
     * <p>
     * 走的是既有的逐音停止路径（{@code PianoBlock.stopSound}），客户端会把对应的那个音收掉，
     * 其余正在响的音不受影响——这正是"抬踏板不该影响还按着的键"的实现方式：
     * 那些键的释放事件排在更后面，压根不会被这次抬踏板碰到。
     */
    private void releaseNote(int tone) {
        if (this.block instanceof PianoBlock pianoBlock) {
            Level level = this.blockEntity.getLevel();
            if (level != null) {
                pianoBlock.stopSound(null, tone, level, this.blockEntity.getBlockPos());
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
    public void play(PianoSongTrack track) {
        if (this.blockEntity.getLevel() == null || this.blockEntity.getLevel().isClientSide) return;
        if (this.isPlaying) return;
        // 只有换歌（或重新开始）才复位进度；同一首歌从暂停中继续时保留播放位置与剩余事件数
        boolean restart = this.track == null || !this.track.name().equals(track.name());
        this.track = track;
        if (restart) {
            this.gameTick = -1;
            this.songTick = -1;
            this.noteCount = track.length();
            LogUtils.getLogger().debug("AutoPlay {}: tempo={} ({} 歌曲刻/秒, {} 游戏刻/歌曲刻), 事件刻数={}",
                    track.name(), track.tempo(),
                    MusicTiming.songTicksPerSecond(track.tempo()),
                    MusicTiming.gameTicksPerSongTick(track.tempo()),
                    track.length());
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
