package com.witcherbb.bettersound.network.protocol.client.piano;

import com.witcherbb.bettersound.network.PacketContext;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record CPianoBlockStopPacket(BlockPos pos, int[] tones) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarIntArray(this.tones);
    }

    public static CPianoBlockStopPacket decode(FriendlyByteBuf buf) {
        return new CPianoBlockStopPacket(buf.readBlockPos(), buf.readVarIntArray());
    }

    public static void handle(CPianoBlockStopPacket packet, PacketContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level != null) {
                ModSoundManager.INSTANCE.tryToStopAllPianoSounds(packet.pos, packet.tones);
            }
        });
    }
}
