package com.witcherbb.bettersound.network.protocol.server;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.blocks.PianoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record SBlockEntityDataChangePacket(BlockPos pos, boolean delay) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.delay);
    }
    public static SBlockEntityDataChangePacket decode(FriendlyByteBuf buf) {
        return new SBlockEntityDataChangePacket(buf.readBlockPos(), buf.readBoolean());
    }
    public static void handle(SBlockEntityDataChangePacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.sender();
            if (sender == null) return;
            Level level = sender.level();
            BlockState state = level.getBlockState(packet.pos);
            if (state.getBlock() instanceof PianoBlock pianoBlock) {
                pianoBlock.setDelay(state, level, packet.pos, packet.delay);
            }
        });
    }
}
