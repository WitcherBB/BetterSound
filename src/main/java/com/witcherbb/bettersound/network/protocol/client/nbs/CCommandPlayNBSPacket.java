package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.exception.NBSNotFoundException;
import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import com.witcherbb.bettersound.music.nbs.NBSAutoPlayer;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CCommandPlayNBSPacket(String filename, BlockPos pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.filename);
        buf.writeBlockPos(this.pos);
    }

    public static CCommandPlayNBSPacket decode(FriendlyByteBuf buf) {
        return new CCommandPlayNBSPacket(buf.readUtf(), buf.readBlockPos());
    }

    public static void handle(CCommandPlayNBSPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            MinecraftExtender extender = (MinecraftExtender) mc;
            if (mc.level == null) return;
            try {
                PianoSong song = extender.betterSound$getNBSLoader().findSong(packet.filename);
                BlockEntity entity = mc.level.getBlockEntity(packet.pos);
                if (entity instanceof NBSAutoPlayer autoPlayer) {
                    autoPlayer.getNBSPlayer().play(song);
                    if (mc.player != null) {
                        mc.player.sendSystemMessage(Component.empty().append(Component.translatable("bettersound.nbs.success", song.fileName)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN));
                    }
                }
            } catch (NBSNotFoundException e) {
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.empty().append(e.getMessage()).withStyle(ChatFormatting.RED));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
