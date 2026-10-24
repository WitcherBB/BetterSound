package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import java.util.function.Supplier;

public record CPianoPlaySingleNotePacket(UUID playerUUID, BlockPos blockPos, Vec3 voicePos, int tone,
                                         byte volume, boolean isForUI, boolean isShort) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.playerUUID == null);
        if (this.playerUUID != null) {
            buf.writeUUID(this.playerUUID);
        }
        buf.writeBlockPos(this.blockPos);
        buf.writeVector3f(this.voicePos.toVector3f());
        buf.writeInt(this.tone);
        buf.writeByte(this.volume);
        buf.writeBoolean(this.isForUI);
        buf.writeBoolean(this.isShort);
    }

    public static CPianoPlaySingleNotePacket decode(FriendlyByteBuf buf) {
        boolean isNull = buf.readBoolean();
        return new CPianoPlaySingleNotePacket(isNull ? null : buf.readUUID(), buf.readBlockPos(),
                new Vec3(buf.readVector3f()), buf.readInt(), buf.readByte(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(CPianoPlaySingleNotePacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.pianoPlaySingleNote(packet));
    }
}
