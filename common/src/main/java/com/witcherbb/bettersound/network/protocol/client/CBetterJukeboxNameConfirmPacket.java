package com.witcherbb.bettersound.network.protocol.client;

import com.witcherbb.bettersound.client.gui.screen.inventory.BetterJukeboxScreen;
import com.witcherbb.bettersound.common.utils.Util;
import com.witcherbb.bettersound.network.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public record CBetterJukeboxNameConfirmPacket(Util.Status status) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.status);
    }

    public static CBetterJukeboxNameConfirmPacket decode(FriendlyByteBuf buf) {
        return new CBetterJukeboxNameConfirmPacket(buf.readEnum(Util.Status.class));
    }

    public static void handle(CBetterJukeboxNameConfirmPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof BetterJukeboxScreen screen) {
                screen.getImageTip().updateStatus(packet.status);
            }
        });
    }
}
