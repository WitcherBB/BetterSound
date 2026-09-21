package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.exception.PlayerIsPlayingMusicException;
import com.witcherbb.bettersound.music.AutoMusicPlayer;
import com.witcherbb.bettersound.music.SustainPolicy;
import com.witcherbb.bettersound.music.bean.PianoSongTrack;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.nbs.SNBSPlayPacket;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * NBS 曲目的自动播放入口。
 *
 * <p>与 MIDI 相比只有两点不同，其余全部在共用的 {@link AutoMusicPlayer} 里：
 * <ol>
 *     <li>客户端用 {@link SNBSPlayPacket} 把曲目投递给服务端；</li>
 *     <li>延音完全按小节线释放（{@link SustainPolicy#BAR_LINE}）。</li>
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
        if (this.player.beginClientPlay(song.fileName)) {
            // 发给服务端数据包
            ModNetwork.sendToServer(new SNBSPlayPacket(this.blockEntity.getBlockPos(), song.fileName, song.getNoteMap(), song.tempo, song.timeSignature));
        }
    }

    /** Server side：由 {@link SNBSPlayPacket} 调用 */
    public void play(PianoSongTrack track) {
        this.player.play(track, SustainPolicy.BAR_LINE);
    }
}
