package com.witcherbb.bettersound.network.protocol.server.nbs;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.music.bean.Note;
import com.witcherbb.bettersound.music.bean.PianoSongTrack;
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
 * @param noteMap 音符表，键是 NBS 歌曲刻下标
 * @param tempo   NBS 原始 tempo：每秒歌曲刻数 × 100
 */
public record SNBSPlayPacket(BlockPos pos, String name, Map<Integer, List<Note>> noteMap, short tempo, byte timeSignature) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(name);
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
        buf.writeShort(tempo);
        buf.writeByte(timeSignature);
    }
    public static SNBSPlayPacket decode(FriendlyByteBuf buf) {
        return new SNBSPlayPacket(buf.readBlockPos(), buf.readUtf(), buf.readMap(
                FriendlyByteBuf::readInt,
                buf1 -> {
                    List<Note> notes = new ArrayList<>();
                    int size = buf1.readInt();
                    for (int i = 0; i < size; i++) {
                        notes.add(Note.decode(buf1.readByteArray()));
                    }
                    return notes;
                }
        ), buf.readShort(), buf.readByte());
    }

    public static void handle(SNBSPlayPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.sender();
            if (sender == null) return;
            Level level = sender.level();
            BlockEntity entity = level.getBlockEntity(packet.pos);
            if (entity instanceof AutoPlayer autoPlayer) {
                autoPlayer.getNBSPlayer().play(new PianoSongTrack(packet.name, packet.noteMap, packet.tempo, packet.timeSignature * 4));
            }
        });
    }
}
