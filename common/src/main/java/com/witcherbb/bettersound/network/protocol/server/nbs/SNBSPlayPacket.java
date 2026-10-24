package com.witcherbb.bettersound.network.protocol.server.nbs;


import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
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
 * 客户端把一首 NBS 交给服务端播放。
 *
 * <p><b>一首曲子会被切成多块发送</b>（单个自定义载荷只有 32767 字节，见
 * {@link MusicScoreTransfer}）：每块都带 {@code chunkIndex}/{@code chunkCount}，
 * 服务端收齐之后拼回整张音符表才开播。
 *
 * @param noteMap      本块携带的音符表，键是 NBS 歌曲刻下标
 * @param tempo        NBS 原始 tempo：每秒歌曲刻数 × 100
 * @param chunkIndex   本块序号，从 0 开始
 * @param chunkCount   一共几块
 */
public record SNBSPlayPacket(BlockPos pos, String name, Map<Integer, List<Note>> noteMap, short tempo,
                             byte timeSignature, int chunkIndex, int chunkCount) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(name);
        buf.writeShort(tempo);
        buf.writeByte(timeSignature);
        buf.writeVarInt(this.chunkIndex);
        buf.writeVarInt(this.chunkCount);
        ScoreWire.writeNotes(buf, this.noteMap);
    }

    public static SNBSPlayPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String name = buf.readUtf();
        short tempo = buf.readShort();
        byte timeSignature = buf.readByte();
        int chunkIndex = buf.readVarInt();
        int chunkCount = buf.readVarInt();
        return new SNBSPlayPacket(pos, name, ScoreWire.readNotes(buf), tempo, timeSignature, chunkIndex, chunkCount);
    }

    public static void handle(SNBSPlayPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Player sender = ctx.get().getPlayer();
            if (sender == null) return;
            // 分块投递：收齐了才有完整的音符表，否则先攒着
            Map<Integer, List<Note>> merged = MusicScoreTransfer.accept(sender.getUUID(),
                    packet.chunkIndex, packet.chunkCount, packet.noteMap);
            if (merged == null) return;
            Level level = sender.level();
            BlockEntity entity = level.getBlockEntity(packet.pos);
            if (entity instanceof AutoPlayer autoPlayer) {
                autoPlayer.getNBSPlayer().play(new PianoSongTrack(packet.name, merged, packet.tempo, packet.timeSignature));
            }
        });
    }
}
