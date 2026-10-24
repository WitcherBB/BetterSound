package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

public record CCommandPlayNBSPacket(String filename, BlockPos pos) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.filename);
        buf.writeBlockPos(this.pos);
    }

    public static CCommandPlayNBSPacket decode(FriendlyByteBuf buf) {
        return new CCommandPlayNBSPacket(buf.readUtf(), buf.readBlockPos());
    }

    public static void handle(CCommandPlayNBSPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.commandPlayNBS(packet));
    }
}
