package com.witcherbb.bettersound.network.protocol.server.piano;

import com.witcherbb.bettersound.blocks.PianoBlock;

import com.witcherbb.bettersound.blocks.AbstractPianoBlock;
import com.witcherbb.bettersound.common.ModToneManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public record SPianoKeyPressedPacket(BlockPos pos, int tone, byte volume) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(tone);
        buf.writeByte(volume);
    }

    public static SPianoKeyPressedPacket decode(FriendlyByteBuf buf) {
        return new SPianoKeyPressedPacket(buf.readBlockPos(), buf.readInt(), buf.readByte());
    }

    public static void handle(SPianoKeyPressedPacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Player sender = ctx.get().getPlayer();
            if (sender == null) return;
            Level level = sender.level();
            BlockPos pos = packet.pos;
            BlockState state = level.getBlockState(pos);
            state = state.setValue(AbstractPianoBlock.TONE, packet.tone);
            level.setBlock(pos, state, AbstractPianoBlock.UPDATE_ALL);
            if (state.getBlock() instanceof AbstractPianoBlock pianoBlock) {
                pianoBlock.playSound((ServerPlayer) sender, packet.tone, packet.volume, level, pos, false);
                BlockPos mainPos = state.getBlock() instanceof PianoBlock
                        ? PianoBlock.getSectionPos(state, pos, PianoBlock.MIDDEL_PART) : pos;
                ModToneManager.getInstance().putLastTone(mainPos, sender.getUUID(), packet.tone());
            }
        });
    }
}
