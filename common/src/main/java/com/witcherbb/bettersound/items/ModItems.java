package com.witcherbb.bettersound.items;

import com.witcherbb.bettersound.BetterSoundCommon;
import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.custom.RecordItemSupporter;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
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
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Constants.MOD_ID, Registries.ITEM);
    public static final List<RegistrySupplier<Item>> music_discs_list = new ArrayList<>();

    public static final RegistrySupplier<Item> ITEM_BETTER_JUKEBOX =
            registerBlockItem("better_jukebox", ModBlocks.BETTER_JUKEBOX_BLOCK);
    public static final RegistrySupplier<Item> ITEM_JUKEBOX_CONTROLLER =
            registerBlockItem("jukebox_controller", ModBlocks.JUKEBOX_CONTROLLER);
    public static final RegistrySupplier<Item> ITEM_EXAMPLE_BLOCK_ITEM =
            registerBlockItem("example_block", ModBlocks.EXAMPLE_BLOCK);
    public static final RegistrySupplier<Item> ITEM_PIANO_BLOCK =
            registerBlockItem("piano_block", ModBlocks.PIANO_BLOCK);
    public static final RegistrySupplier<Item> ITEM_TONE_BLOCK =
            registerBlockItem("tone_block", ModBlocks.TONE_BLOCK);
    public static final RegistrySupplier<Item> ITEM_PIANO_STOOL_BLOCK =
            registerBlockItem("piano_stool_block", ModBlocks.PIANO_STOOL_BLOCK);
    public static final RegistrySupplier<Item> ITEM_SUSTAIN_PEDAL =
            registerBlockItem("sustain_pedal", ModBlocks.SUSTAIN_PEDAL);
    public static final RegistrySupplier<Item> ITEM_TUNER =
            ITEMS.register("tuner", TunerItem::new);
    public static final RegistrySupplier<Item> ITEM_WHITE_KEY =
            registerNormalItem("white_key");
    public static final RegistrySupplier<Item> ITEM_BLACK_KEY =
            registerNormalItem("black_key");
    public static final RegistrySupplier<Item> ITEM_KEYBOARD =
            registerNormalItem("keyboard");
    public static final RegistrySupplier<Item> ITEM_PIANO_VOICE_CORE =
            registerNormalItem("piano_voice_core");

    public static void register() {
        BetterSoundCommon.RECORD_ITEM_REGISTRY.forEach(supporter -> {
            music_discs_list.addAll(supporter.registerItems(ITEMS));
        });
        ITEMS.register();
    }

    private static RegistrySupplier<Item> registerBlockItem(String name, Supplier<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistrySupplier<Item> registerNormalItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private ModItems() {
    }
}
