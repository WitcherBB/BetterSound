package com.witcherbb.bettersound.network.protocol;

import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CPianoBlockStopPacket(BlockPos pos, int[] tones) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarIntArray(this.tones);
    }

    public static CPianoBlockStopPacket decode(FriendlyByteBuf buf) {
        return new CPianoBlockStopPacket(buf.readBlockPos(), buf.readVarIntArray());
    }

    public static void handle(CPianoBlockStopPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level != null) {
                ((MinecraftExtender) minecraft).betterSound$getmodSoundManager().tryToStopAllPianoSounds(packet.pos, packet.tones);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
