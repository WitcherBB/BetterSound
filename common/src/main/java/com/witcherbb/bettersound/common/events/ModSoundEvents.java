package com.witcherbb.bettersound.common.events;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

/** 音效登记表（含 88 键的钢琴音源）。 */
public final class ModSoundEvents {

    public static RegistryRef<SoundEvent> MOD_MUSIC_SCHOOL_SONG;
    public static RegistryRef<SoundEvent> MOD_MUSIC_HAPPY_FLY_TO_FORWARD;
    public static RegistryRef<SoundEvent> MOD_MUSIC_EXCITED1;
    public static RegistryRef<SoundEvent> MOD_MUSIC_EXCITED2;
    public static RegistryRef<SoundEvent> MOD_MUSIC_AWARDING;
    public static RegistryRef<SoundEvent> MOD_MUSIC_ENDING;
    public static RegistryRef<SoundEvent> MOD_MUSIC_SMALL_TOWN;
    public static RegistryRef<SoundEvent> MOD_MUSIC_SYA;
    public static RegistryRef<SoundEvent> MOD_MUSIC_LDCXQ;

    /** 钢琴音源，索引即音调（tone）。 */
    public static final List<RegistryRef<SoundEvent>> pianoSounds = new ArrayList<>();

    private static final List<String> TONES = buildTones();

    public static void register(ModRegistrar registrar) {
        MOD_MUSIC_SCHOOL_SONG = registerSoundEvent(registrar, "music.school_song");
        MOD_MUSIC_HAPPY_FLY_TO_FORWARD = registerSoundEvent(registrar, "music.happy_to_fly_forward");
        MOD_MUSIC_EXCITED1 = registerSoundEvent(registrar, "music.excited1");
        MOD_MUSIC_EXCITED2 = registerSoundEvent(registrar, "music.excited2");
        MOD_MUSIC_AWARDING = registerSoundEvent(registrar, "music.awarding");
        MOD_MUSIC_ENDING = registerSoundEvent(registrar, "music.ending");
        MOD_MUSIC_SMALL_TOWN = registerSoundEvent(registrar, "music.small_town");
        MOD_MUSIC_SYA = registerSoundEvent(registrar, "music.sya");
        MOD_MUSIC_LDCXQ = registerSoundEvent(registrar, "music.ldcxq");

        for (String tone : TONES) {
            pianoSounds.add(registerSoundEvent(registrar, "note.mda_piano.note_%s".formatted(tone)));
        }
    }

    private static RegistryRef<SoundEvent> registerSoundEvent(ModRegistrar registrar, String name) {
        return registrar.register(Registries.SOUND_EVENT, name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.MOD_ID, name)));
    }

    private static List<String> buildTones() {
        final List<String> tones = new ArrayList<>();
        tones.add("a0");
        tones.add("a0_rise");
        tones.add("b0");
        for (int i = 1; i <= 7; i++) {
            tones.add("c%d".formatted(i));
            tones.add("c%d_rise".formatted(i));
            tones.add("d%d".formatted(i));
            tones.add("d%d_rise".formatted(i));
            tones.add("e%d".formatted(i));
            tones.add("f%d".formatted(i));
            tones.add("f%d_rise".formatted(i));
            tones.add("g%d".formatted(i));
            tones.add("g%d_rise".formatted(i));
            tones.add("a%d".formatted(i));
            tones.add("a%d_rise".formatted(i));
            tones.add("b%d".formatted(i));
        }
        tones.add("c8");
        return tones;
    }

    private ModSoundEvents() {
    }
}
