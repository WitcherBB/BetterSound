package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import com.witcherbb.bettersound.music.nbs.NBSPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CNBSPausePacket(BlockPos pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static CNBSPausePacket decode(FriendlyByteBuf buf) {
        return new CNBSPausePacket(buf.readBlockPos());
    }

    public static void handle(CNBSPausePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if (level != null) {
                BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                if (blockEntity instanceof AutoPlayer autoPlayer) {
                    NBSPlayer nbsPlayer = autoPlayer.getNBSPlayer();
                    if (!nbsPlayer.hasSong() && mc.player != null) {
                        mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.hasnosong").withStyle(ChatFormatting.RED));
                    } else if (!autoPlayer.getNBSPlayer().isPlaying() && mc.player != null) {
                        mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotplaying", nbsPlayer.getSongName()).withStyle(ChatFormatting.RED));
                    } else {
                        nbsPlayer.pause();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
