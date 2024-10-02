package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record CToneBlockPlayNotePacket(UUID playerUUID, BlockPos pos, int tone, boolean stop, boolean isForUI) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.playerUUID == null);
        if (this.playerUUID != null) {
            buf.writeUUID(this.playerUUID);
        }
        buf.writeBlockPos(this.pos);
        buf.writeByte(this.tone);
        buf.writeBoolean(this.stop);
        buf.writeBoolean(this.isForUI);
    }

    public static CToneBlockPlayNotePacket decode(FriendlyByteBuf buf) {
        boolean isNull = buf.readBoolean();
        return new CToneBlockPlayNotePacket(isNull ? null : buf.readUUID(), buf.readBlockPos(), buf.readByte(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(CToneBlockPlayNotePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ModSoundManager soundManager = ((MinecraftExtender) Minecraft.getInstance()).getmodSoundManager();
            if (Minecraft.getInstance().level != null) {
                BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(packet.pos);
                if (packet.stop) {
                    soundManager.tryToStopPianoSound(packet.playerUUID, packet.pos, packet.tone);
                }
                else {
                    if (entity instanceof ToneBlockEntity blockEntity)
                        soundManager.playPianoSound(ModSoundEvents.pianoSounds.get(packet.tone).get(), packet.playerUUID, packet.pos, packet.tone, packet.isForUI, !blockEntity.isSoundDelay());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
