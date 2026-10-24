package com.witcherbb.bettersound.network.protocol.client;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

public record COpenPianoScreenPacket(BlockPos pos) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static COpenPianoScreenPacket decode(FriendlyByteBuf buf) {
        return new COpenPianoScreenPacket(buf.readBlockPos());
    }

    public static void handle(COpenPianoScreenPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.openPianoScreen(packet));
    }
}
