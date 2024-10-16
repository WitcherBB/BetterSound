package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CNBSReloadPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static CNBSReloadPacket decode(FriendlyByteBuf buf) {
        return new CNBSReloadPacket();
    }

    public static void handle(CNBSReloadPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            MinecraftExtender mcExtender = (MinecraftExtender) mc;
            mcExtender.betterSound$getNBSLoader().load();
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.translatable("bettersound.nbs.loadscs").withStyle(ChatFormatting.GREEN));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
