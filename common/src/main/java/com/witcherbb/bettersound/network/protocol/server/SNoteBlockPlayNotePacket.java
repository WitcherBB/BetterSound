package com.witcherbb.bettersound.network.protocol.server;

import com.witcherbb.bettersound.network.PacketContext;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public record SNoteBlockPlayNotePacket(int note, BlockPos pos) {

    public static SNoteBlockPlayNotePacket decode(FriendlyByteBuf buf) {
        return new SNoteBlockPlayNotePacket(buf.readByte(), buf.readBlockPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(this.note);
        buf.writeBlockPos(this.pos);
    }

    public static void handle(SNoteBlockPlayNotePacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.sender();
            if (sender == null) return;

            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            int _new = com.witcherbb.bettersound.common.platform.Platform.hooks().onNoteChange(level, pos, state, state.getValue(NoteBlock.NOTE), packet.note);
            if (_new == -1) return;
            state = state.setValue(NoteBlock.NOTE, _new);
            level.setBlock(pos, state, NoteBlock.UPDATE_ALL);
            if (state.getValue(NoteBlock.INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir()) {
                level.blockEvent(pos, state.getBlock(), 0, 0);
                level.gameEvent(sender, GameEvent.NOTE_BLOCK_PLAY, pos);
            }
        });
    }
}
