package com.witcherbb.bettersound.common.events;

import com.witcherbb.bettersound.BetterSoundCommon;
import com.witcherbb.bettersound.Constants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

/** 音效登记表（含 88 键的钢琴音源）。 */
public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Constants.MOD_ID, Registries.SOUND_EVENT);

//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_SCHOOL_SONG =
//            registerSoundEvent("music.school_song");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_HAPPY_FLY_TO_FORWARD =
//            registerSoundEvent("music.happy_to_fly_forward");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_EXCITED1 =
//            registerSoundEvent("music.excited1");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_EXCITED2 =
//            registerSoundEvent("music.excited2");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_AWARDING =
//            registerSoundEvent("music.awarding");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_ENDING =
//            registerSoundEvent("music.ending");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_SMALL_TOWN =
//            registerSoundEvent("music.small_town");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_SYA =
//            registerSoundEvent("music.sya");
//    public static final RegistrySupplier<SoundEvent> MOD_MUSIC_LDCXQ =
//            registerSoundEvent("music.ldcxq");

    /** 钢琴音源，索引即音调（tone）。 */
    public static final List<RegistrySupplier<SoundEvent>> pianoSounds = new ArrayList<>();
    private static final List<String> TONES = buildTones();

    static {
        for (String tone : TONES) {
            pianoSounds.add(registerSoundEvent("note.mda_piano.note_%s".formatted(tone)));
        }
    }

    public static void register() {
        BetterSoundCommon.RECORD_ITEM_REGISTRY.forEach(supporter -> {
            supporter.registerSoundEvents(SOUND_EVENTS);
        });
        SOUND_EVENTS.register();
    }

    private static RegistrySupplier<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.MOD_ID, name)));
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
