package com.witcherbb.bettersound.music.midi;

import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.AutoMusicPlayer;
import com.witcherbb.bettersound.music.bean.PianoSongTrack;
import com.witcherbb.bettersound.music.midi.bean.MidiSong;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.midi.SMidiPlayPacket;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * MIDI 曲目的自动播放入口。
 *
 * <p>与 NBS 相比只有两点不同，其余全部在共用的 {@link AutoMusicPlayer} 里：
 * <ol>
 *     <li>客户端先把解析好的 MIDI 编译成引擎认识的 {@link PianoSongTrack}，
 *         再用 {@link SMidiPlayPacket} 投递给服务端（见 {@link MidiTiming}）；</li>
 *     <li>延音跟随文件里记录的 CC64 踏板，见 {@link PedalSustainPolicy}。</li>
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
        PianoSongTrack track = song.compile();
        if (this.player.beginClientPlay(track.name())) {
            ModNetwork.sendToServer(new SMidiPlayPacket(this.blockEntity.getBlockPos(), track.name(),
                    track.noteMap(), track.subsectionLength(), track.pedalChanges()));
        }
    }

    /** Server side：由 {@link SMidiPlayPacket} 调用 */
    public void play(PianoSongTrack track) {
        this.player.play(track, new PedalSustainPolicy(track.pedalChanges(), maxSustainTicks(track)));
    }

    /**
     * 踏板安全阀的时长上限：一小节多少游戏刻 × 配置的小节数。
     * <p>没有踏板时间轴时安全阀没有意义（引擎会回退到小节线释放），此时不去读配置。
     */
    private static int maxSustainTicks(PianoSongTrack track) {
        if (track.pedalChanges().isEmpty()) return 0;
        return com.witcherbb.bettersound.common.config.Configs.midiMaxSustainBars() * track.subsectionLength();
    }
}
