package com.witcherbb.bettersound.network.protocol.server.midi;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
import com.witcherbb.bettersound.music.midi.MidiTiming;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * @param name             曲目名
 * @param noteMap          事件表，键是游戏刻
 * @param subsectionLength 一小节多少游戏刻
 * @param releaseOnBarLine 是否要引擎按小节线释放延音（文件里没有踏板信息时才为 true）
 */
public record SMidiPlayPacket(BlockPos pos, String name, Map<Integer, List<Note>> noteMap, int subsectionLength,
                              boolean releaseOnBarLine) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.name);
        buf.writeMap(this.noteMap,
                FriendlyByteBuf::writeInt,
                (buf1, notes) -> {
                    int size = notes.size();
                    buf1.writeInt(size);
                    for (int i = 0; i < size; i++) {
                        buf1.writeByteArray(notes.get(i).encode());
                    }
                }
        );
        buf.writeVarInt(this.subsectionLength);
        buf.writeBoolean(this.releaseOnBarLine);
    }

    public static SMidiPlayPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String name = buf.readUtf();
        Map<Integer, List<Note>> noteMap = buf.readMap(
                FriendlyByteBuf::readInt,
                buf1 -> {
                    List<Note> notes = new ArrayList<>();
                    int size = buf1.readInt();
                    for (int i = 0; i < size; i++) {
                        notes.add(Note.decode(buf1.readByteArray()));
                    }
                    return notes;
                }
        );
        int subsectionLength = buf.readVarInt();
        boolean releaseOnBarLine = buf.readBoolean();
        return new SMidiPlayPacket(pos, name, noteMap, subsectionLength, releaseOnBarLine);
    }

    public static void handle(SMidiPlayPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.sender();
            if (sender == null) return;
            Level level = sender.level();
            BlockEntity entity = level.getBlockEntity(packet.pos);
            if (!(entity instanceof AutoPlayer autoPlayer) || autoPlayer.getMidiPlayer() == null) return;
            autoPlayer.getMidiPlayer().play(new PianoSongTrack(packet.name, packet.noteMap,
                    (short) MidiTiming.TEMPO_AS_GAME_TICKS, packet.subsectionLength, packet.releaseOnBarLine));
        });
    }
}
