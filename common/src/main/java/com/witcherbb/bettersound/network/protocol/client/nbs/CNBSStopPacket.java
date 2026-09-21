package com.witcherbb.bettersound.network.protocol.client.nbs;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.music.AutoMusicPlayer;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record CNBSStopPacket(BlockPos pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static CNBSStopPacket decode(FriendlyByteBuf buf) {
        return new CNBSStopPacket(buf.readBlockPos());
    }

    public static void handle(CNBSStopPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level != null) {
                BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                if (blockEntity instanceof AutoPlayer autoPlayer) {
                    // 播放状态只有一份（在共用引擎里），所以 NBS 与 MIDI 用的是同一套同步包
                    AutoMusicPlayer musicPlayer = autoPlayer.getMusicPlayer();
                    if (!musicPlayer.hasSong() && mc.player != null) {
                        mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.hasnosong").withStyle(ChatFormatting.RED));
                    } else {
                        musicPlayer.stop();
                    }
                }
            }
        });
    }
}
