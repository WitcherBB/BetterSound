package com.witcherbb.bettersound.client.sound;

import com.witcherbb.bettersound.client.util.PianoSoundMap;
import com.witcherbb.bettersound.client.resources.sounds.PianoSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ModSoundManager {
    private final PianoSoundMap playingPianoNotes = PianoSoundMap.create();
    protected final Minecraft minecraft;

    public ModSoundManager(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void playPianoSound(SoundEvent soundEvent, UUID playerUUID, BlockPos pos, int tone, float volume, boolean isForUI, boolean isShort) {
        this.playPianoSound(soundEvent, playerUUID, Vec3.atCenterOf(pos), tone, volume, isForUI, isShort);
    }

    public void playPianoSound(SoundEvent soundEvent, UUID playerUUID, Vec3 pos, int tone, float volume, boolean isForUI, boolean isShort) {
        PianoSoundInstance soundInstance;
        BlockPos blockPos = BlockPos.containing(pos);
        if (isForUI) {
            soundInstance = PianoSoundInstance.forUI(soundEvent, blockPos, Vec3.ZERO, playerUUID, tone, volume);
        } else {
            soundInstance = PianoSoundInstance.forPosition(soundEvent, pos, playerUUID, tone, volume, isShort);
        }
        minecraft.getSoundManager().play(soundInstance);
        playingPianoNotes.put(blockPos, playerUUID, tone, soundInstance);
    }

    private void stopPianoSound(UUID playerUUID, BlockPos pos, int tone) {
        PianoSoundInstance soundInstance = playingPianoNotes.removeFirst(pos, playerUUID, tone);
        if (soundInstance != null)
            soundInstance.setStoped();
    }

    public void tryToStopPianoSound(UUID playerUUID, BlockPos pos, int tone) {
        if (playingPianoNotes.getFirst(pos, playerUUID, tone) == null) return;
        this.stopPianoSound(playerUUID, pos, tone);
    }

    public void tryToStopAllPianoSounds(BlockPos pos, int[] exceptedTones) {
        List<PianoSoundInstance> instances = playingPianoNotes.removeAllButLast(pos, exceptedTones);
        if (instances.isEmpty()) return;
        for (int i = 0; i < instances.size(); i++) {
            instances.get(i).setStoped();
        }
    }

    public PianoSoundMap getPlayingPianoNotes() {
        return playingPianoNotes;
    }

    public void stopAll() {
        this.playingPianoNotes.clear();
    }

    public void destroy() {
        this.stopAll();
    }
}
