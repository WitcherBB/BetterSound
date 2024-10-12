package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
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

public record CPianoBlockPlayMultipleNotesPacket(UUID playerUUID, BlockPos pos, byte[] tones, byte[] volumes, boolean stop) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.playerUUID == null);
        if (this.playerUUID != null) {
            buf.writeUUID(this.playerUUID);
        }
        buf.writeBlockPos(this.pos);
        buf.writeByteArray(this.tones);
        buf.writeByteArray(this.volumes);
        buf.writeBoolean(this.stop);
    }

    public static CPianoBlockPlayMultipleNotesPacket decode(FriendlyByteBuf buf) {
        boolean isNull = buf.readBoolean();
        return new CPianoBlockPlayMultipleNotesPacket(isNull ? null : buf.readUUID(), buf.readBlockPos(), buf.readByteArray(), buf.readByteArray(), buf.readBoolean());
    }

    public static void handle(CPianoBlockPlayMultipleNotesPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ModSoundManager soundManager = ((MinecraftExtender) Minecraft.getInstance()).betterSound$getmodSoundManager();
            if (Minecraft.getInstance().level != null) {
                BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(packet.pos);
                for (int i = 0; i < packet.tones.length; i++) {
                    byte tone = packet.tones[i];
                    if (tone == -1) continue;
                    if (packet.stop) {
                        soundManager.tryToStopAllPianoSounds(packet.pos, new int[0]);
                    }
                    else if (entity instanceof PianoBlockEntity blockEntity){
                        soundManager.playPianoSoundWithVolume(ModSoundEvents.pianoSounds.get(tone).get(), packet.volumes[i] * 3.0F / 100, packet.playerUUID, packet.pos, packet.tones[i], !blockEntity.isSoundDelay());
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
