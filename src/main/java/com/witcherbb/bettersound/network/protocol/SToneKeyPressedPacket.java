package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.blocks.ToneBlock;
import com.witcherbb.bettersound.mixins.extenders.MinecraftServerExtender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public record SToneKeyPressedPacket(BlockPos pos, int tone) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(tone);
    }

    public static SToneKeyPressedPacket decode(FriendlyByteBuf buf) {
        return new SToneKeyPressedPacket(buf.readBlockPos(), buf.readByte());
    }

    public static void handle(SToneKeyPressedPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            state = state.setValue(ToneBlock.TONE, packet.tone);
            level.setBlock(pos, state, ToneBlock.UPDATE_ALL);
            if (state.getBlock() instanceof ToneBlock toneBlock) {
                toneBlock.playSound(sender, state, level, pos);
                ((MinecraftServerExtender) ServerLifecycleHooks.getCurrentServer()).betterSound$getModDataManager().putLastTone(packet.pos, sender.getUUID(), packet.tone());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
