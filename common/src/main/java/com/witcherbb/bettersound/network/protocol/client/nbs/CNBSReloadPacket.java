package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record CNBSReloadPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static CNBSReloadPacket decode(FriendlyByteBuf buf) {
        return new CNBSReloadPacket();
    }

    public static void handle(CNBSReloadPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            ModSoundManager.INSTANCE.getNbsLoader().load();
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.translatable("bettersound.nbs.loadscs").withStyle(ChatFormatting.GREEN));
            }
        });
    }
}
