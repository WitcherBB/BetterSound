package com.witcherbb.bettersound.network.protocol.server.piano;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.blocks.AbstractPianoBlock;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.common.ModToneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record SPianoKeyReleasedPacket(BlockPos pos, int tone, boolean whetherToStop) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(tone);
        buf.writeBoolean(whetherToStop);
    }

    public static SPianoKeyReleasedPacket decode(FriendlyByteBuf buf) {
        return new SPianoKeyReleasedPacket(buf.readBlockPos(), buf.readByte(), buf.readBoolean());
    }

    public static void handle(SPianoKeyReleasedPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.sender();
            if (sender == null) return;
            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractPianoBlock pianoBlock) {
                if (packet.whetherToStop) pianoBlock.stopSound(sender, packet.tone, level, pos);
                // 簿记键必须和 SPianoKeyPressedPacket 里 putLastTone 用的一致（钢琴统一到键盘中格），
                // 否则这一条删不掉：账只增不减，整片切时会把这些陈旧条目当成"还按着的音"保留下来，
                // 结果就是那些音再也停不掉。
                BlockPos mainPos = state.getBlock() instanceof PianoBlock
                        ? PianoBlock.getSectionPos(state, pos, PianoBlock.MIDDEL_PART) : pos;
                ModToneManager.getInstance().removeLastTone(mainPos, sender.getUUID(), packet.tone());
            }
        });
    }
}
