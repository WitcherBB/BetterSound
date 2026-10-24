package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.AutoMusicPlayer;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.MusicScoreTransfer;
import com.witcherbb.bettersound.network.protocol.server.nbs.SNBSPlayPacket;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Map;

/**
 * NBS 曲目的自动播放入口。
 *
 * <p>与 MIDI 相比只有两点不同，其余全部在共用的 {@link AutoMusicPlayer} 里：
 * <ol>
 *     <li>客户端用 {@link SNBSPlayPacket} 把曲目投递给服务端；</li>
 *     <li>曲子里没有释放事件（NBS 格式不记录时值），延音释放点由引擎按小节线给出
 *         （{@link PianoSongTrack#releaseOnBarLine()} 为 true）。</li>
 * </ol>
 * 对照实现见 {@code com.witcherbb.bettersound.music.midi.MidiPlayer}。
 */
public class NBSPlayer {
    private final BlockEntity blockEntity;
    private final AutoMusicPlayer player;

    public NBSPlayer(BlockEntity blockEntity, AutoMusicPlayer player) {
        this.blockEntity = blockEntity;
        this.player = player;
    }

    /** Client side */
    public void play(PianoSong song) throws PlayerIsPlayingMusicException {
        if (!this.player.beginClientPlay(song.fileName)) return;
        // 大一点的 NBS 同样会撞单个载荷上限，和 MIDI 一样按刻切块发
        List<Map<Integer, List<Note>>> chunks = MusicScoreTransfer.split(song.getNoteMap());
        for (int index = 0; index < chunks.size(); index++) {
            ModNetwork.sendToServer(new SNBSPlayPacket(this.blockEntity.getBlockPos(), song.fileName,
                    chunks.get(index), song.tempo, song.timeSignature, index, chunks.size()));
        }
    }

    /** Server side：由 {@link SNBSPlayPacket} 调用 */
    public void play(PianoSongTrack track) {
        this.player.play(track);
    }
}
