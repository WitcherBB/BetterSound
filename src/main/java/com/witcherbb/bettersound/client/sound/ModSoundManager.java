package com.witcherbb.bettersound.client.sound;

import com.witcherbb.bettersound.client.util.PianoSoundMap;
import com.witcherbb.bettersound.client.resources.sounds.PianoSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ModSoundManager {
    private final PianoSoundMap playingPianoNotes = PianoSoundMap.create();
    protected final Minecraft minecraft;

    public ModSoundManager(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void playPianoSound(SoundEvent soundEvent, UUID playerUUID, BlockPos pos, Integer tone, boolean isForUI, boolean isShort) {
        PianoSoundInstance soundInstance;
        if (isForUI) {
            soundInstance = PianoSoundInstance.forUI(soundEvent, pos, playerUUID, tone);
        } else {
            soundInstance = PianoSoundInstance.forBlock(soundEvent, pos, playerUUID, tone, isShort);
        }
        minecraft.getSoundManager().play(soundInstance);
        playingPianoNotes.put(pos, playerUUID, tone, soundInstance);
    }

    private void stopPianoSound(UUID playerUUID, BlockPos pos, Integer tone) {
        PianoSoundInstance soundInstance = playingPianoNotes.removeFirst(pos, playerUUID, tone);
        if (soundInstance != null)
            soundInstance.setStoped();
    }

    public void tryToStopPianoSound(UUID playerUUID, BlockPos pos, Integer tone) {
        if (playingPianoNotes.get(pos, playerUUID, tone) == null) return;
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
