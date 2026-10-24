package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.network.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record CPianoStopSingleNotePacket(UUID playerUUID, BlockPos playingPos, int tone) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.playerUUID == null);
        if (this.playerUUID != null) {
            buf.writeUUID(this.playerUUID);
        }
        buf.writeBlockPos(this.playingPos);
        buf.writeVarInt(tone);
    }

    public static CPianoStopSingleNotePacket decode(FriendlyByteBuf buf) {
        boolean isUUIDNull = buf.readBoolean();
        return new CPianoStopSingleNotePacket(isUUIDNull ? null : buf.readUUID(), buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(CPianoStopSingleNotePacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ModSoundManager.INSTANCE.tryToStopPianoSound(packet.playerUUID, packet.playingPos, packet.tone);
        });
    }
}
