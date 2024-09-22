package com.witcherbb.bettersound.common.events;

import com.witcherbb.bettersound.BetterSound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUNDEVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BetterSound.MODID);
    public static final RegistryObject<SoundEvent> MOD_MUSIC_SCHOOL_SONG = registerSoundEvent("music.school_song");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_HAPPY_FLY_TO_FORWARD = registerSoundEvent("music.happy_to_fly_forward");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_EXCITED1 = registerSoundEvent("music.excited1");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_EXCITED2 = registerSoundEvent("music.excited2");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_AWARDING = registerSoundEvent("music.awarding");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_ENDING = registerSoundEvent("music.ending");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_SMALL_TOWN = registerSoundEvent("music.small_town");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_SYA = registerSoundEvent("music.sya");
    public static final RegistryObject<SoundEvent> MOD_MUSIC_LDCXQ = registerSoundEvent("music.ldcxq");

    public static final List<RegistryObject<SoundEvent>> pianoSounds = new ArrayList<>();

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDEVENTS.register(name,
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BetterSound.MODID, name)));
    }

    static {
        final List<String> TONES = new ArrayList<>();
        TONES.add("a0");
        TONES.add("a0_rise");
        TONES.add("b0");
        for (int i = 1; i <= 7; i++) {
            TONES.add("c%d".formatted(i));
            TONES.add("c%d_rise".formatted(i));
            TONES.add("d%d".formatted(i));
            TONES.add("d%d_rise".formatted(i));
            TONES.add("e%d".formatted(i));
            TONES.add("f%d".formatted(i));
            TONES.add("f%d_rise".formatted(i));
            TONES.add("g%d".formatted(i));
            TONES.add("g%d_rise".formatted(i));
            TONES.add("a%d".formatted(i));
            TONES.add("a%d_rise".formatted(i));
            TONES.add("b%d".formatted(i));
        }
        TONES.add("c8");

        int size = TONES.size();
        for (int i = 0; i < size; i++) {
            String name = "note.mda_piano.note_%s".formatted(TONES.get(i));
            pianoSounds.add(registerSoundEvent(name));
        }
    }
}
