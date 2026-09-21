package com.witcherbb.bettersound.network.protocol.client;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.client.gui.screen.inventory.JukeboxScreen;
import com.witcherbb.bettersound.common.utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public record CJukeboxNameConfirmPacket(Util.Status status) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.status);
    }

    public static CJukeboxNameConfirmPacket decode(FriendlyByteBuf buf) {
        return new CJukeboxNameConfirmPacket(buf.readEnum(Util.Status.class));
    }

    public static void handle(CJukeboxNameConfirmPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof JukeboxScreen jukeboxScreen) {
                jukeboxScreen.getImageTip().updateStatus(packet.status);
            }
        });
    }
}
