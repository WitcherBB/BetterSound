package com.witcherbb.bettersound.items;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BetterSound.MODID);

	public static List<RegistryObject<Item>> music_discs_list = new ArrayList<>();
	/* ********************************************** 这 里 加 唱 片 ********************************************************* */

	public static final RegistryObject<Item> SCHOOL_SONG_MUSIC_DISC = registerMusicDisc("school_song",
			ModSoundEvents.MOD_MUSIC_SCHOOL_SONG, 190);
	public static final RegistryObject<Item> HAPPY_TO_FLY_FORWARD_MUSIC_DISC = registerMusicDisc("happy_to_fly_forward",
			ModSoundEvents.MOD_MUSIC_HAPPY_FLY_TO_FORWARD, 78);
	public static final RegistryObject<Item> EXCITED1_MUSIC_DISC = registerMusicDisc("excited1",
			ModSoundEvents.MOD_MUSIC_EXCITED1, 136);
	public static final RegistryObject<Item> EXCITED2_MUSIC_DISC = registerMusicDisc("excited2",
			ModSoundEvents.MOD_MUSIC_EXCITED2, 243);
	public static final RegistryObject<Item> AWARDING_MUSIC_DISC = registerMusicDisc("awarding",
			ModSoundEvents.MOD_MUSIC_AWARDING, 61);
	public static final RegistryObject<Item> ENDING_MUSIC_DISC = registerMusicDisc("ending",
			ModSoundEvents.MOD_MUSIC_ENDING, 232);
	public static final RegistryObject<Item> SMALL_TOWN_MUSIC_DISC = registerMusicDisc("small_town",
			ModSoundEvents.MOD_MUSIC_SMALL_TOWN, 270);
	public static final RegistryObject<Item> SEE_YOU_AGAIN_MUSIC_DISC = registerMusicDisc("see_you_again",
			ModSoundEvents.MOD_MUSIC_SYA, 229);
	public static final RegistryObject<Item> LDCXQ_MUSIC_DISC = registerMusicDisc("ldcxq",
			ModSoundEvents.MOD_MUSIC_LDCXQ, 114);

	/* ********************************************************************************************************************* */
	public static final RegistryObject<Item> ITEM_JUKEBOX_CONTROLLER = registerBlockItem(ModBlocks.JUKEBOX_CONTROLLER, new Item.Properties());
	public static final RegistryObject<Item> ITEM_EXAMPLE_BLOCK_ITEM = registerBlockItem(ModBlocks.EXAMPLE_BLOCK, new Item.Properties());
	public static final RegistryObject<Item> ITEM_PIANO_BLOCK = registerBlockItem(ModBlocks.PIANO_BLOCK, new Item.Properties());
	public static final RegistryObject<Item> ITEM_TONE_BLOCK = registerBlockItem(ModBlocks.TONE_BLOCK, new Item.Properties());
	public static final RegistryObject<Item> ITEM_PIANO_STOOL_BLOCK = registerBlockItem(ModBlocks.PIANO_STOOL_BLOCK, new Item.Properties());
	public static final RegistryObject<Item> ITEM_SUSTAIN_PEDAL = registerBlockItem(ModBlocks.SUSTAIN_PEDAL, new Item.Properties());

	public static final RegistryObject<Item> ITEM_TUNER = ITEMS.register("tuner", TunerItem::new);

	public static final RegistryObject<Item> ITEM_WHITE_KEY = registerNormalItem("white_key", new Item.Properties());
	public static final RegistryObject<Item> ITEM_BLACK_KEY = registerNormalItem("black_key", new Item.Properties());
	public static final RegistryObject<Item> ITEM_KEYBOARD = registerNormalItem("keyboard", new Item.Properties());
	public static final RegistryObject<Item> ITEM_PIANO_VOICE_CORE = registerNormalItem("piano_voice_core", new Item.Properties());

	private static RegistryObject<Item> registerMusicDisc(String music_name, Supplier<SoundEvent> soundSupplier, int delay_second) {
		RegistryObject<Item> registryObject = ITEMS.register(music_name + "_music_disc",
				() -> new RecordItem(15, soundSupplier,
						new Item.Properties().stacksTo(1).rarity(Rarity.RARE), delay_second * 20));
		music_discs_list.add(registryObject);
		return registryObject;
	}

	private static RegistryObject<Item> registerBlockItem(RegistryObject<Block> block, Item.Properties properties) {
		return ITEMS.register(block.getId().getPath(),
				() -> new BlockItem(block.get(), properties));
	}

	private static RegistryObject<Item> registerNormalItem(String name, Item.Properties properties) {
		return ITEMS.register(name, () -> new Item(properties));
	}
}
