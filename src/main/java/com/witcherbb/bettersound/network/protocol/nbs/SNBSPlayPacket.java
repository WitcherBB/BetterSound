package com.witcherbb.bettersound.network.protocol.nbs;

import com.witcherbb.bettersound.music.nbs.NBSAutoPlayer;
import com.witcherbb.bettersound.music.nbs.bean.Note;
import com.witcherbb.bettersound.music.nbs.bean.PianoSongTrack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record SNBSPlayPacket(BlockPos pos, Map<Integer, List<Note>> noteMap, short speed, byte timeSignature) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
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
        buf.writeShort(speed);
        buf.writeByte(timeSignature);
    }
    public static SNBSPlayPacket decode(FriendlyByteBuf buf) {
        return new SNBSPlayPacket(buf.readBlockPos(), buf.readMap(
                FriendlyByteBuf::readInt,
                buf1 -> {
                    List<Note> notes = new ArrayList<>();
                    int size = buf1.readInt();
                    for (int i = 0; i < size; i++) {
                        notes.add(Note.decode(buf1.readByteArray()));
                    }
                    return notes;
                }
        ), buf.readByte(), buf.readByte());
    }

    public static void handle(SNBSPlayPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            Level level = sender.level();
            BlockEntity entity = level.getBlockEntity(packet.pos);
            if (entity instanceof NBSAutoPlayer nbsAutoPlayer) {
                nbsAutoPlayer.getNBSPlayer().play(new PianoSongTrack(packet.noteMap, packet.speed, packet.timeSignature * 4));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
