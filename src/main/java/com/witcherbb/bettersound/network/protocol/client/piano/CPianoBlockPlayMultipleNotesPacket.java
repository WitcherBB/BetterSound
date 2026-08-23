package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.music.nbs.bean.Note;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public record CPianoBlockPlayMultipleNotesPacket(UUID playerUUID, BlockPos sourcePos, Vec3[] positions, Note[] notes) {
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

    public static CPianoBlockPlayMultipleNotesPacket decode(FriendlyByteBuf buf) {
        int posLength = buf.readVarInt();
        Vec3[] positions = new Vec3[posLength];
        for (int i = 0; i < posLength; i++) {
            positions[i] = new Vec3(buf.readVector3f());
        }
        boolean isNull = buf.readBoolean();
        UUID playerUUID = isNull ? null : buf.readUUID();
        BlockPos sourcePos = buf.readBlockPos();
        byte[] noteData = buf.readByteArray();
        // 每个 Note 定长 Note.FIELD_NUM(3) 字节（pitch/volume/layer），按固定步长切片解码。
        // 旧实现用 while(!isEmpty(noteData)) 且从未重写 noteData，导致解码死循环卡死网络线程。
        int noteCount = noteData.length / Note.FIELD_NUM;
        Note[] notes = new Note[noteCount];
        for (int i = 0; i < noteCount; i++) {
            notes[i] = Note.decode(Arrays.copyOfRange(noteData, i * Note.FIELD_NUM, (i + 1) * Note.FIELD_NUM));
        }
        return new CPianoBlockPlayMultipleNotesPacket(playerUUID, sourcePos, positions, notes);
    }

    public static void handle(CPianoBlockPlayMultipleNotesPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                ModSoundManager soundManager = ModSoundManager.INSTANCE;
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(packet.sourcePos);
                if (packet.positions.length != packet.notes.length) {
                    return;
                }
                for (int i = 0; i < packet.notes.length; i++) {
                    Note note = packet.notes[i];
                    // 过滤无效音调（旧版解析器残留的 -1 哨兵等），防止 pianoSounds.get() 越界崩溃
                    if (note.getPitch() < 0 || note.getPitch() >= ModSoundEvents.pianoSounds.size()) {
                        continue;
                    }
                    if (blockEntity instanceof PianoBlockEntity pianoBlockEntity){
                        soundManager.playPianoSound(ModSoundEvents.pianoSounds.get(note.getPitch()).get(), packet.playerUUID,
                                packet.positions[i], note.getPitch(), Note.toPianoSoundVolume(note.getVolume()), false, !pianoBlockEntity.isSoundDelay());
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
