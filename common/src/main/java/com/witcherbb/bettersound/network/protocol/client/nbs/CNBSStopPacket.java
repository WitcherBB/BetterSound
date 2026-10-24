package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

public record CNBSStopPacket(BlockPos pos) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static CNBSStopPacket decode(FriendlyByteBuf buf) {
        return new CNBSStopPacket(buf.readBlockPos());
    }

    public static void handle(CNBSStopPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.nbsStop(packet));
    }
}
