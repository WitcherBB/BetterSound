package com.witcherbb.bettersound.network.protocol.client.nbs;

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
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CNBSPlayOnPacket(BlockPos pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static CNBSPlayOnPacket decode(FriendlyByteBuf buf) {
        return new CNBSPlayOnPacket(buf.readBlockPos());
    }

    public static void handle(CNBSPlayOnPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level != null) {
                BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                if (blockEntity instanceof AutoPlayer autoPlayer) {
                    AutoMusicPlayer musicPlayer = autoPlayer.getMusicPlayer();
                    if (!musicPlayer.hasSong() && mc.player != null) {
                        mc.player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.hasnosong").withStyle(ChatFormatting.RED));
                    } else if (musicPlayer.isPlaying() && mc.player != null) {
                        mc.player.sendSystemMessage(Component.translatable("bettersound.nbs.success", musicPlayer.getSongName()).withStyle(ChatFormatting.RED));
                    } else {
                        musicPlayer.playOn();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
