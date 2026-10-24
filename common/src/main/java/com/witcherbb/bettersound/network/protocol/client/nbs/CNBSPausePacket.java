package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

public record CNBSPausePacket(BlockPos pos) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static CNBSPausePacket decode(FriendlyByteBuf buf) {
        return new CNBSPausePacket(buf.readBlockPos());
    }

    public static void handle(CNBSPausePacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.nbsPause(packet));
    }
}
