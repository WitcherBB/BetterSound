package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

public record CNBSReloadPacket() {

    public void encode(FriendlyByteBuf buf) {
    }

    public static CNBSReloadPacket decode(FriendlyByteBuf buf) {
        return new CNBSReloadPacket();
    }

    public static void handle(CNBSReloadPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.nbsReload(packet));
    }
}
