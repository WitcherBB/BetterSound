package com.witcherbb.bettersound.network.protocol.client;

import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.client.gui.screen.inventory.PianoBlockScreen;
import com.witcherbb.bettersound.client.gui.screen.inventory.ToneBlockScreen;
import com.witcherbb.bettersound.network.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public record COpenPianoScreenPacket(BlockPos pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static COpenPianoScreenPacket decode(FriendlyByteBuf buf) {
        return new COpenPianoScreenPacket(buf.readBlockPos());
    }

    public static void handle(COpenPianoScreenPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            BlockEntity blockEntity = mc.level.getBlockEntity(packet.pos);
            if (blockEntity instanceof PianoBlockEntity) {
                mc.setScreen(new PianoBlockScreen(packet.pos));
            } else if (blockEntity instanceof ToneBlockEntity) {
                mc.setScreen(new ToneBlockScreen(packet.pos));
            }
        });
    }
}
