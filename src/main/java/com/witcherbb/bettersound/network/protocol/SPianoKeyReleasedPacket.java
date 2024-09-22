package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.mixins.extenders.MinecraftServerExtender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public record SPianoKeyReleasedPacket(BlockPos pos, int tone, boolean whetherToStop) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(tone);
        buf.writeBoolean(whetherToStop);
    }

    public static SPianoKeyReleasedPacket decode(FriendlyByteBuf buf) {
        return new SPianoKeyReleasedPacket(buf.readBlockPos(), buf.readByte(), buf.readBoolean());
    }

    public static void handle(SPianoKeyReleasedPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof PianoBlock pianoBlock) {
                if (packet.whetherToStop) pianoBlock.stopSound(sender, state, level, pos);
                ((MinecraftServerExtender) ServerLifecycleHooks.getCurrentServer()).getModDataManager().removeLastTone(packet.pos, sender.getUUID(), packet.tone());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
