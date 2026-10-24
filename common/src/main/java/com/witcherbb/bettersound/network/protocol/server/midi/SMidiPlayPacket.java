package com.witcherbb.bettersound.network.protocol.server.midi;


import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
import com.witcherbb.bettersound.music.midi.MidiTiming;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import com.witcherbb.bettersound.network.MusicScoreTransfer;
import com.witcherbb.bettersound.network.ScoreWire;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 客户端把编译好的 MIDI 曲目交给服务端播放。
 *
 * <p>MIDI 的时间轴在客户端就已经折算到游戏刻（见 {@link MidiTiming}），所以这里传的事件表
 * 与 NBS 的 {@link com.witcherbb.bettersound.network.protocol.server.nbs.SNBSPlayPacket}
 * 结构一致，服务端拿到后直接喂给同一个播放引擎。tempo 固定为
 * {@link MidiTiming#TEMPO_AS_GAME_TICKS}，不需要占用网络字段。
 *
 * <p>事件表里可能夹着"释放延音"的哨兵（{@link Note#KIND_RELEASE_ALL} 等）——它们是服务端才知道的东西，
 * 引擎派发时会先分流出去，转成既有的演奏/停止数据包，客户端不会看到。
 *
 * <p><b>一首曲子会被切成多块发送</b>（单个自定义载荷只有 32767 字节，见
 * {@link MusicScoreTransfer}）：每块都带 {@code chunkIndex}/{@code chunkCount}，
 * 服务端收齐之后拼回整张事件表才开播。
 *
 * @param name             曲目名
 * @param noteMap          本块携带的事件表，键是游戏刻
 * @param subsectionLength 一小节多少游戏刻
 * @param releaseOnBarLine 是否要引擎按小节线释放延音
 * @param chunkIndex       本块序号，从 0 开始
 * @param chunkCount       一共几块
 */
public record SMidiPlayPacket(BlockPos pos, String name, Map<Integer, List<Note>> noteMap, int subsectionLength,
                              boolean releaseOnBarLine, int chunkIndex, int chunkCount) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.name);
        buf.writeVarInt(this.subsectionLength);
        buf.writeBoolean(this.releaseOnBarLine);
        buf.writeVarInt(this.chunkIndex);
        buf.writeVarInt(this.chunkCount);
        ScoreWire.writeNotes(buf, this.noteMap);
    }

    public static SMidiPlayPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String name = buf.readUtf();
        int subsectionLength = buf.readVarInt();
        boolean releaseOnBarLine = buf.readBoolean();
        int chunkIndex = buf.readVarInt();
        int chunkCount = buf.readVarInt();
        return new SMidiPlayPacket(pos, name, ScoreWire.readNotes(buf), subsectionLength, releaseOnBarLine,
                chunkIndex, chunkCount);
    }

    public static void handle(SMidiPlayPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Player sender = ctx.get().getPlayer();
            if (sender == null) return;
            // 分块投递：收齐了才有完整的事件表，否则先攒着
            Map<Integer, List<Note>> merged = MusicScoreTransfer.accept(sender.getUUID(),
                    packet.chunkIndex, packet.chunkCount, packet.noteMap);
            if (merged == null) return;
            Level level = sender.level();
            BlockEntity entity = level.getBlockEntity(packet.pos);
            if (!(entity instanceof AutoPlayer autoPlayer) || autoPlayer.getMidiPlayer() == null) return;
            autoPlayer.getMidiPlayer().play(new PianoSongTrack(packet.name, merged,
                    (short) MidiTiming.TEMPO_AS_GAME_TICKS, packet.subsectionLength, packet.releaseOnBarLine));
        });
    }
}
