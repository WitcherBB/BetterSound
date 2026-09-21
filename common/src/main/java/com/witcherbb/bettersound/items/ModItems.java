package com.witcherbb.bettersound.items;

import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** 物品登记表。 */
public final class ModItems {

    public static final List<RegistryRef<Item>> music_discs_list = new ArrayList<>();

    public static RegistryRef<Item> SCHOOL_SONG_MUSIC_DISC;
    public static RegistryRef<Item> HAPPY_TO_FLY_FORWARD_MUSIC_DISC;
    public static RegistryRef<Item> EXCITED1_MUSIC_DISC;
    public static RegistryRef<Item> EXCITED2_MUSIC_DISC;
    public static RegistryRef<Item> AWARDING_MUSIC_DISC;
    public static RegistryRef<Item> ENDING_MUSIC_DISC;
    public static RegistryRef<Item> SMALL_TOWN_MUSIC_DISC;
    public static RegistryRef<Item> SEE_YOU_AGAIN_MUSIC_DISC;
    public static RegistryRef<Item> LDCXQ_MUSIC_DISC;

    public static RegistryRef<Item> ITEM_JUKEBOX_CONTROLLER;
    public static RegistryRef<Item> ITEM_EXAMPLE_BLOCK_ITEM;
    public static RegistryRef<Item> ITEM_PIANO_BLOCK;
    public static RegistryRef<Item> ITEM_TONE_BLOCK;
    public static RegistryRef<Item> ITEM_PIANO_STOOL_BLOCK;
    public static RegistryRef<Item> ITEM_SUSTAIN_PEDAL;
    public static RegistryRef<Item> ITEM_TUNER;
    public static RegistryRef<Item> ITEM_WHITE_KEY;
    public static RegistryRef<Item> ITEM_BLACK_KEY;
    public static RegistryRef<Item> ITEM_KEYBOARD;
    public static RegistryRef<Item> ITEM_PIANO_VOICE_CORE;

    /** 由各 loader 的入口在注册阶段调用一次（必须先于 ModBlocks/ModSoundEvents 的对应内容完成登记顺序）。 */
    public static void register(ModRegistrar registrar) {
        /* ********************************************** 这 里 加 唱 片 ********************************************************* */
        SCHOOL_SONG_MUSIC_DISC = registerMusicDisc(registrar, "school_song", ModSoundEvents.MOD_MUSIC_SCHOOL_SONG, 190);
        HAPPY_TO_FLY_FORWARD_MUSIC_DISC = registerMusicDisc(registrar, "happy_to_fly_forward", ModSoundEvents.MOD_MUSIC_HAPPY_FLY_TO_FORWARD, 78);
        EXCITED1_MUSIC_DISC = registerMusicDisc(registrar, "excited1", ModSoundEvents.MOD_MUSIC_EXCITED1, 136);
        EXCITED2_MUSIC_DISC = registerMusicDisc(registrar, "excited2", ModSoundEvents.MOD_MUSIC_EXCITED2, 243);
        AWARDING_MUSIC_DISC = registerMusicDisc(registrar, "awarding", ModSoundEvents.MOD_MUSIC_AWARDING, 61);
        ENDING_MUSIC_DISC = registerMusicDisc(registrar, "ending", ModSoundEvents.MOD_MUSIC_ENDING, 232);
        SMALL_TOWN_MUSIC_DISC = registerMusicDisc(registrar, "small_town", ModSoundEvents.MOD_MUSIC_SMALL_TOWN, 270);
        SEE_YOU_AGAIN_MUSIC_DISC = registerMusicDisc(registrar, "see_you_again", ModSoundEvents.MOD_MUSIC_SYA, 229);
        LDCXQ_MUSIC_DISC = registerMusicDisc(registrar, "ldcxq", ModSoundEvents.MOD_MUSIC_LDCXQ, 114);
        /* ********************************************************************************************************************* */

        ITEM_JUKEBOX_CONTROLLER = registerBlockItem(registrar, "jukebox_controller", ModBlocks.JUKEBOX_CONTROLLER);
        ITEM_EXAMPLE_BLOCK_ITEM = registerBlockItem(registrar, "example_block", ModBlocks.EXAMPLE_BLOCK);
        ITEM_PIANO_BLOCK = registerBlockItem(registrar, "piano_block", ModBlocks.PIANO_BLOCK);
        ITEM_TONE_BLOCK = registerBlockItem(registrar, "tone_block", ModBlocks.TONE_BLOCK);
        ITEM_PIANO_STOOL_BLOCK = registerBlockItem(registrar, "piano_stool_block", ModBlocks.PIANO_STOOL_BLOCK);
        ITEM_SUSTAIN_PEDAL = registerBlockItem(registrar, "sustain_pedal", ModBlocks.SUSTAIN_PEDAL);

        ITEM_TUNER = registrar.register(Registries.ITEM, "tuner", TunerItem::new);

        ITEM_WHITE_KEY = registerNormalItem(registrar, "white_key");
        ITEM_BLACK_KEY = registerNormalItem(registrar, "black_key");
        ITEM_KEYBOARD = registerNormalItem(registrar, "keyboard");
        ITEM_PIANO_VOICE_CORE = registerNormalItem(registrar, "piano_voice_core");
    }

    private static RegistryRef<Item> registerMusicDisc(ModRegistrar registrar, String name, Supplier<SoundEvent> soundSupplier, int delaySecond) {
        RegistryRef<Item> item = registrar.register(Registries.ITEM, name + "_music_disc",
                // vanilla 的 RecordItem 构造器要的是 SoundEvent（Forge 才被 patch 成 Supplier），
                // 物品注册事件晚于音效注册事件，所以这里直接取已注册好的实例
                () -> new RecordItem(15, soundSupplier.get(), new Item.Properties().stacksTo(1).rarity(Rarity.RARE), delaySecond * 20));
        music_discs_list.add(item);
        return item;
    }

    private static RegistryRef<Item> registerBlockItem(ModRegistrar registrar, String name, RegistryRef<Block> block) {
        return registrar.register(Registries.ITEM, name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryRef<Item> registerNormalItem(ModRegistrar registrar, String name) {
        return registrar.register(Registries.ITEM, name, () -> new Item(new Item.Properties()));
    }

    private ModItems() {
    }
}
