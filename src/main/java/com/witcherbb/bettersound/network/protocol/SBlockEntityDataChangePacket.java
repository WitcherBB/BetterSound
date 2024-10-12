package com.witcherbb.bettersound.network.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SBlockEntityDataChangePacket(BlockPos pos, CompoundTag nbt) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeNbt(this.nbt);
    }
    public static SBlockEntityDataChangePacket decode(FriendlyByteBuf buf) {
        return new SBlockEntityDataChangePacket(buf.readBlockPos(), buf.readNbt());
    }
    public static void handle(SBlockEntityDataChangePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            Level level = sender.level();
            BlockEntity blockEntity = level.getBlockEntity(packet.pos);
            if (blockEntity == null || blockEntity.isRemoved()) return;
            blockEntity.deserializeNBT(packet.nbt);
            blockEntity.setChanged();
        });
        ctx.get().setPacketHandled(true);
    }
}
