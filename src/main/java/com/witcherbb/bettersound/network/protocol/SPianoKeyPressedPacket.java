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

public record SPianoKeyPressedPacket(BlockPos pos, int tone) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(tone);
    }

    public static SPianoKeyPressedPacket decode(FriendlyByteBuf buf) {
        return new SPianoKeyPressedPacket(buf.readBlockPos(), buf.readByte());
    }

    public static void handle(SPianoKeyPressedPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            state = state.setValue(PianoBlock.TONE, packet.tone);
            level.setBlock(pos, state, PianoBlock.UPDATE_ALL);
            if (state.getBlock() instanceof PianoBlock pianoBlock) {
                pianoBlock.playSound(sender, state, level, pos);
                ((MinecraftServerExtender) ServerLifecycleHooks.getCurrentServer()).getModDataManager().putLastTone(packet.pos, sender.getUUID(), packet.tone());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
