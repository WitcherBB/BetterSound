package com.witcherbb.bettersound.network.protocol.client;

import com.witcherbb.bettersound.client.network.ClientPacketHandlers;
import com.witcherbb.bettersound.common.utils.Util;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

public record CBetterJukeboxNameConfirmPacket(Util.Status status) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.status);
    }

    public static CBetterJukeboxNameConfirmPacket decode(FriendlyByteBuf buf) {
        return new CBetterJukeboxNameConfirmPacket(buf.readEnum(Util.Status.class));
    }

    public static void handle(CBetterJukeboxNameConfirmPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> ClientPacketHandlers.betterJukeboxNameConfirm(packet));
    }
}
