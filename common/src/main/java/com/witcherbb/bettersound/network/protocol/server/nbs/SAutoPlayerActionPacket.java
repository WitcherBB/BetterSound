package com.witcherbb.bettersound.network.protocol.server.nbs;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record SAutoPlayerActionPacket(BlockPos pos, Action action) {
    public enum Action {
        PLAY_ON,
        STOP,
        PAUSE;

        private static final Action[] VALUES = values();

        public static Action fromId(int id) {
            return VALUES[id];
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeEnum(this.action);
    }

    public static SAutoPlayerActionPacket decode(FriendlyByteBuf buf) {
        return new SAutoPlayerActionPacket(buf.readBlockPos(), buf.readEnum(Action.class));
    }

    public static void handle(SAutoPlayerActionPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.sender();
            if (player == null) return;
            Level level = player.level();
            BlockPos target = packet.pos;
            BlockState state = level.getBlockState(target);
            if (state.getBlock() instanceof PianoBlock) {
                target = PianoBlock.getVoiceSectionPos(state, target, PianoBlock.MIDDEL_PART);
            }
            if (level.getBlockEntity(target) instanceof AutoPlayer autoPlayer) {
                switch (packet.action) {
                    case PLAY_ON -> autoPlayer.playNBSOn();
                    case STOP -> autoPlayer.stopNBS();
                    case PAUSE -> autoPlayer.pauseNBS();
                }
            } else {
                player.sendSystemMessage(Component.translatable("wrong.bettersound.nbs.isnotautoplayer").withStyle(ChatFormatting.RED));
            }
        });
    }
}
