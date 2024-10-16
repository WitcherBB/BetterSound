package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import com.witcherbb.bettersound.music.nbs.bean.Note;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record CPianoBlockPlayNotePacket(UUID playerUUID, Vec3 pos, int tone, byte volume, boolean stop, boolean isForUI) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.playerUUID == null);
        if (this.playerUUID != null) {
            buf.writeUUID(this.playerUUID);
        }

        buf.writeVector3f(this.pos.toVector3f());
        buf.writeInt(this.tone);
        buf.writeByte(this.volume);
        buf.writeBoolean(this.stop);
        buf.writeBoolean(this.isForUI);
    }

    public static CPianoBlockPlayNotePacket decode(FriendlyByteBuf buf) {
        boolean isNull = buf.readBoolean();
        return new CPianoBlockPlayNotePacket(isNull ? null : buf.readUUID(), new Vec3(buf.readVector3f()), buf.readInt(), buf.readByte(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(CPianoBlockPlayNotePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ModSoundManager soundManager = ((MinecraftExtender) Minecraft.getInstance()).betterSound$getmodSoundManager();
            BlockPos blockPos = BlockPos.containing(packet.pos);
            if (Minecraft.getInstance().level != null) {
                BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(blockPos);
                if (packet.stop) {
                    soundManager.tryToStopPianoSound(packet.playerUUID, blockPos, packet.tone);
                }
                else {
                    if (entity instanceof PianoBlockEntity blockEntity)
                        soundManager.playPianoSound(ModSoundEvents.pianoSounds.get(packet.tone).get(), packet.playerUUID, packet.pos, packet.tone, Note.toPianoSoundVolume(packet.volume), packet.isForUI, !blockEntity.isSoundDelay());
                }
            }

        });
        ctx.get().setPacketHandled(true);
    }
}
