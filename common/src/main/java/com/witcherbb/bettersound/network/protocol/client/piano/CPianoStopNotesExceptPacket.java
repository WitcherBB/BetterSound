package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

public record CPianoStopNotesExceptPacket(BlockPos pos, int[] tones) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarIntArray(this.tones);
    }

    public static CPianoStopNotesExceptPacket decode(FriendlyByteBuf buf) {
        return new CPianoStopNotesExceptPacket(buf.readBlockPos(), buf.readVarIntArray());
    }

    public static void handle(CPianoStopNotesExceptPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.pianoStopNotesExcept(packet));
    }
}
