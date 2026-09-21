package com.witcherbb.bettersound.network.protocol.server.piano;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.blocks.AbstractPianoBlock;
import com.witcherbb.bettersound.common.ModToneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record SPianoKeyPressedPacket(BlockPos pos, int tone, byte volume) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(tone);
        buf.writeByte(volume);
    }

    public static SPianoKeyPressedPacket decode(FriendlyByteBuf buf) {
        return new SPianoKeyPressedPacket(buf.readBlockPos(), buf.readInt(), buf.readByte());
    }

    public static void handle(SPianoKeyPressedPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.sender();
            if (sender == null) return;
            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            state = state.setValue(AbstractPianoBlock.TONE, packet.tone);
            level.setBlock(pos, state, AbstractPianoBlock.UPDATE_ALL);
            if (state.getBlock() instanceof AbstractPianoBlock pianoBlock) {
                pianoBlock.playSound(sender, packet.tone, packet.volume, level, pos);
                ModToneManager.getInstance().putLastTone(packet.pos, sender.getUUID(), packet.tone());
            }
        });
    }
}
