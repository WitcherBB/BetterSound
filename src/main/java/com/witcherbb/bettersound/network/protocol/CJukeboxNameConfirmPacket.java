package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.client.gui.screen.inventory.JukeboxScreen;
import com.witcherbb.bettersound.common.utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CJukeboxNameConfirmPacket {
    private final Util.Status status;

    public CJukeboxNameConfirmPacket(Util.Status status) {
        this.status = status;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.status);
    }

    public static CJukeboxNameConfirmPacket decode(FriendlyByteBuf buf) {
        return new CJukeboxNameConfirmPacket(buf.readEnum(Util.Status.class));
    }

    public static void handle(CJukeboxNameConfirmPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof JukeboxScreen jukeboxScreen) {
                jukeboxScreen.getImageTip().updateStatus(packet.status);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
