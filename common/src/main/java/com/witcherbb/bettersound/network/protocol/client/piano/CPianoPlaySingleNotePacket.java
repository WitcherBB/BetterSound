package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.blocks.AbstractPianoBlock;
import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.music.data.Note;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record CPianoPlaySingleNotePacket(UUID playerUUID, BlockPos blockPos, Vec3 voicePos, int tone,
                                         byte volume, boolean isForUI, boolean isShort) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.playerUUID == null);
        if (this.playerUUID != null) {
            buf.writeUUID(this.playerUUID);
        }

        buf.writeBlockPos(this.blockPos);
        buf.writeVector3f(this.voicePos.toVector3f());
        buf.writeInt(this.tone);
        buf.writeByte(this.volume);
        buf.writeBoolean(this.isForUI);
        buf.writeBoolean(this.isShort);
    }

    public static CPianoPlaySingleNotePacket decode(FriendlyByteBuf buf) {
        boolean isNull = buf.readBoolean();
        return new CPianoPlaySingleNotePacket(isNull ? null : buf.readUUID(), buf.readBlockPos(),
                new Vec3(buf.readVector3f()), buf.readInt(), buf.readByte(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(CPianoPlaySingleNotePacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            ModSoundManager soundManager = ModSoundManager.INSTANCE;
            BlockState blockState = level.getBlockState(packet.blockPos);
            if (blockState.getBlock() instanceof AbstractPianoBlock) {
                soundManager.playPianoSound(ModSoundEvents.pianoSounds.get(packet.tone).get(), packet.playerUUID,
                        packet.blockPos, packet.voicePos, packet.tone, Note.toPianoSoundVolume(packet.volume),
                        packet.isForUI, packet.isShort);
            }
        });
    }
}
