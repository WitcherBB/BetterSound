package com.witcherbb.bettersound.network.protocol.server;


import com.witcherbb.bettersound.blocks.PianoBlock;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public record SBlockEntityDataChangePacket(BlockPos pos, boolean delay) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.delay);
    }
    public static SBlockEntityDataChangePacket decode(FriendlyByteBuf buf) {
        return new SBlockEntityDataChangePacket(buf.readBlockPos(), buf.readBoolean());
    }
    public static void handle(SBlockEntityDataChangePacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Player sender = ctx.get().getPlayer();
            if (sender == null) return;
            Level level = sender.level();
            BlockState state = level.getBlockState(packet.pos);
            if (state.getBlock() instanceof PianoBlock pianoBlock) {
                pianoBlock.setDelay(state, level, packet.pos, packet.delay);
            }
        });
    }
}
