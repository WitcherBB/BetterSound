package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import com.witcherbb.bettersound.music.data.Note;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public record CPianoPlayMultipleNotesPacket(UUID playerUUID, BlockPos sourcePos, Vec3[] positions, Note[] notes) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.positions.length);
        for (Vec3 pos : this.positions) {
            buf.writeVector3f(pos.toVector3f());
        }
        buf.writeBoolean(this.playerUUID == null);
        if (this.playerUUID != null) {
            buf.writeUUID(this.playerUUID);
        }
        buf.writeBlockPos(sourcePos);
        ByteArrayOutputStream byteBuf = new ByteArrayOutputStream();
        for (Note note : this.notes) {
            byteBuf.writeBytes(note.encode());
        }
        buf.writeByteArray(byteBuf.toByteArray());
    }

    public static CPianoPlayMultipleNotesPacket decode(FriendlyByteBuf buf) {
        int posLength = buf.readVarInt();
        Vec3[] positions = new Vec3[posLength];
        for (int i = 0; i < posLength; i++) {
            positions[i] = new Vec3(buf.readVector3f());
        }
        boolean isNull = buf.readBoolean();
        UUID playerUUID = isNull ? null : buf.readUUID();
        BlockPos sourcePos = buf.readBlockPos();
        byte[] noteData = buf.readByteArray();
        // 每个 Note 定长 Note.FIELD_NUM 字节（pitch/volume/layer/duration），按固定步长切片解码。
        // 旧实现用 while(!isEmpty(noteData)) 且从未重写 noteData，导致解码死循环卡死网络线程。
        int noteCount = noteData.length / Note.FIELD_NUM;
        Note[] notes = new Note[noteCount];
        for (int i = 0; i < noteCount; i++) {
            notes[i] = Note.decode(Arrays.copyOfRange(noteData, i * Note.FIELD_NUM, (i + 1) * Note.FIELD_NUM));
        }
        return new CPianoPlayMultipleNotesPacket(playerUUID, sourcePos, positions, notes);
    }

    public static void handle(CPianoPlayMultipleNotesPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.pianoPlayMultipleNotes(packet));
    }
}
