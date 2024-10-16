package com.witcherbb.bettersound.client.resources.sounds;

import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import com.witcherbb.bettersound.mixins.extenders.SoundInstanceExtender;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyReleasedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class PianoSoundInstance extends AbstractTickableSoundInstance implements SoundInstanceExtender {
    private int tickCount = 0;
    private int lastTick = 0;
    private boolean volumeAttenuation;
    private boolean isShort;
    private boolean wasSet;
    private short shortDelay;
    private int tone;
    private UUID playerUUID;
    private BlockPos pos;
    private final float firstVolume;

    private static final float FULL_VOLUME = 3.0F;

    public static PianoSoundInstance forUI(SoundEvent soundEvent, BlockPos pos, Vec3 relative, UUID playerUUID, int tone) {
        return forUI(soundEvent, pos, relative, playerUUID, tone, FULL_VOLUME);
    }

    public static PianoSoundInstance forUI(SoundEvent soundEvent, BlockPos pos, Vec3 relative, UUID playerUUID, int tone, float volume) {
        return forUIRelative(soundEvent, pos, Vec3.ZERO, playerUUID, tone, volume);
    }

    public static PianoSoundInstance forUIRelative(SoundEvent soundEvent, BlockPos pos, Vec3 relative, UUID playerUUID, int tone, float volume) {
        PianoSoundInstance instance = new PianoSoundInstance(soundEvent, volume, 1.0F, relative.x, relative.y, relative.z, true, false);
        instance.setPos(pos);
        instance.setPlayerUUID(playerUUID);
        instance.setTone(tone);
        return instance;
    }

    public static PianoSoundInstance forBlock(SoundEvent soundEvent, BlockPos pos, UUID playerUUID, int tone, boolean isShort) {
        return forPosition(soundEvent, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, playerUUID, tone, FULL_VOLUME, isShort);
    }

    public static PianoSoundInstance forBlock(SoundEvent soundEvent, BlockPos pos, UUID playerUUID, int tone, float volume, boolean isShort) {
        return forPosition(soundEvent, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, playerUUID, tone, volume, isShort);
    }

    public static PianoSoundInstance forPosition(SoundEvent soundEvent, Vec3 vec3, UUID playerUUID, int tone, float volume, boolean isShort) {
        return forPosition(soundEvent, vec3.x, vec3.y, vec3.z, playerUUID, tone, volume, isShort);
    }

    public static PianoSoundInstance forPosition(SoundEvent soundEvent, double x, double y, double z, UUID playerUUID, int tone, float volume, boolean isShort) {
        PianoSoundInstance instance = new PianoSoundInstance(soundEvent, volume, 1.0F, x, y, z, false, isShort);
        instance.setPlayerUUID(playerUUID);
        instance.setTone(tone);
        return instance;
    }

    private PianoSoundInstance(SoundEvent soundEvent, float volume, float pitch, double x, double y, double z, boolean relative, boolean isShort) {
        super(soundEvent, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.volume = volume;
        this.firstVolume = volume;
        this.pitch = pitch;
        this.relative = relative;
        this.x = x;
        this.y = y;
        this.z = z;
        if (!this.relative) this.pos = BlockPos.containing(this.x, this.y, this.z);
        this.isShort = isShort;
        this.shortDelay = 2;
    }

    @Override
    public void tick() {
        this.tickCount++;
        if (volumeAttenuation) {
            this.volume += this.getDeltaVolume(this.tickCount - this.lastTick);
            if (this.volume <= 0) {
                this.stop();
            }
        }
        if (isShort && (--shortDelay <= 0)) {
            this.setStoped();
            this.isShort = false;
        }
    }

    public void setStoped() {
        this.wasSet = true;
        this.lastTick = this.tickCount;
        this.volumeAttenuation = true;
    }

    private float getDeltaVolume(int tick) {
        return -1.74F / (tick * tick) * this.firstVolume / FULL_VOLUME - 0.015F;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setTone(int tone) {
        this.tone = tone;
    }

    @Override
    public void betterSound$onStop() {
        //判断是否是手动停止很有必要！！！这样就不会在声音彻底停止以后再清除一次，否则可能会把清除之前播放的另一个音清除掉！！！
        if (!this.wasSet) {
            ((MinecraftExtender) Minecraft.getInstance()).betterSound$getmodSoundManager().getPlayingPianoNotes().removeFirst(this.pos, this.playerUUID, this.tone);
        }
        ModNetwork.sendToServer(new SPianoKeyReleasedPacket(pos, this.tone, true));
    }
}
