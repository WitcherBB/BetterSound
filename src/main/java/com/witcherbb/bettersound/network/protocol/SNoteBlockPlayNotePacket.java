package com.witcherbb.bettersound.network.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SNoteBlockPlayNotePacket {
    private final int note;
    private final BlockPos pos;

    public SNoteBlockPlayNotePacket(int note, BlockPos pos) {
        this.note = note;
        this.pos = pos;
    }

    public SNoteBlockPlayNotePacket(FriendlyByteBuf buf) {
        this(buf.readByte(), buf.readBlockPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(this.note);
        buf.writeBlockPos(this.pos);
    }

    public static void handle(SNoteBlockPlayNotePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            int _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NoteBlock.NOTE), packet.note);
            if (_new == -1) return;
            state = state.setValue(NoteBlock.NOTE, _new);
            level.setBlock(pos, state, NoteBlock.UPDATE_ALL);
            if (state.getValue(NoteBlock.INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir()) {
                level.blockEvent(pos, state.getBlock(), 0, 0);
                level.gameEvent(sender, GameEvent.NOTE_BLOCK_PLAY, pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
