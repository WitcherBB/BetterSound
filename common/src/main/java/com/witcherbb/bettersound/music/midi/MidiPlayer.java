package com.witcherbb.bettersound.music.midi;

import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.AutoMusicPlayer;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
import com.witcherbb.bettersound.music.midi.bean.MidiSong;
import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.MusicScoreTransfer;
import com.witcherbb.bettersound.network.protocol.server.midi.SMidiPlayPacket;
import com.witcherbb.bettersound.platform.CommonConfig;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Map;

/**
 * MIDI 曲目的自动播放入口。
 *
 * <p>与 NBS 相比只有两点不同，其余全部在共用的 {@link AutoMusicPlayer} 里：
 * <ol>
 *     <li>客户端先把解析好的 MIDI 编译成引擎认识的 {@link PianoSongTrack}，
 *         再用 {@link SMidiPlayPacket} 投递给服务端（见 {@link MidiTiming}）；</li>
 *     <li>延音释放点全部在<b>编译期</b>就折成了事件表里的哨兵（见 {@link MidiSong#compile(int)}），
 *         服务端不需要任何额外状态。</li>
 * </ol>
 * 对照实现见 {@code com.witcherbb.bettersound.music.nbs.NBSPlayer}。
 */
public class MidiPlayer {
    private final BlockEntity blockEntity;
    private final AutoMusicPlayer player;

    public MidiPlayer(BlockEntity blockEntity, AutoMusicPlayer player) {
        this.blockEntity = blockEntity;
        this.player = player;
    }

    /** Client side：编译后把播放请求发给服务端 */
    public void play(MidiSong song) throws PlayerIsPlayingMusicException {
        // 先做互斥检查再做（相对耗时的）编译，避免白白编译一遍
        if (this.player.isPlaying()) throw new PlayerIsPlayingMusicException();
        PianoSongTrack track = song.compile(maxSustainTicks(song));
        if (!this.player.beginClientPlay(track.name())) return;
        // 一首曲子常常远超单个载荷上限（32767 字节），按刻切成多块发；服务端收齐之后才开播
        List<Map<Integer, List<Note>>> chunks = MusicScoreTransfer.split(track.noteMap());
        for (int index = 0; index < chunks.size(); index++) {
            ModNetwork.sendToServer(new SMidiPlayPacket(this.blockEntity.getBlockPos(), track.name(),
                    chunks.get(index), track.subsectionLength(), track.releaseOnBarLine(), index, chunks.size()));
        }
    }

    /** Server side：由 {@link SMidiPlayPacket} 调用 */
    public void play(PianoSongTrack track) {
        this.player.play(track);
    }

    /**
     * 踏板安全阀的时长上限：一小节多少游戏刻 × 配置的小节数。
     * <p>放在客户端算，是因为释放点要在编译期就折进事件表；0 表示不限制。
     */
    private static int maxSustainTicks(MidiSong song) {
        return Math.max(0, CommonConfig.midiMaxSustainBars()) * Math.max(1, song.barGameTicks);
    }
}
