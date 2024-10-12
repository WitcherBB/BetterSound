package com.witcherbb.bettersound.network.protocol.nbs;

import com.witcherbb.bettersound.music.nbs.NBSAutoPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CNBSStopPacket(BlockPos pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static CNBSStopPacket decode(FriendlyByteBuf buf) {
        return new CNBSStopPacket(buf.readBlockPos());
    }

    public static void handle(CNBSStopPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if (level != null) {
                BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                if (blockEntity instanceof NBSAutoPlayer autoPlayer) {
                    if (!autoPlayer.getNBSPlayer().hasSong() && mc.player != null) {
                        mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.hasnosong").withStyle(ChatFormatting.RED));
                    } else {
                        autoPlayer.getNBSPlayer().stop();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
