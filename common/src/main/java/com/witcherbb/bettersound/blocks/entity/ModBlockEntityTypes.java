package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.common.platform.Platform;
import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/** 方块实体类型登记表。 */
public final class ModBlockEntityTypes {

    public static RegistryRef<BlockEntityType<ExampleBlockEntity>> EXAMPLE_BLOCK_ENTITY_TYPE;
    public static RegistryRef<BlockEntityType<JukeboxControllerBlockEntity>> JUKEBOX_CONTROLLER_ENTITY_TYPE;
    public static RegistryRef<BlockEntityType<NoteBlockEntity>> NOTE_BLOCK_ENTITY_TYPE;
    public static RegistryRef<BlockEntityType<PianoBlockEntity>> PIANO_BLOCK_ENTITY_TYPE;
    public static RegistryRef<BlockEntityType<ToneBlockEntity>> TONE_BLOCK_ENTITY_TYPE;
    public static RegistryRef<BlockEntityType<PianoStoolBlockEntity>> PIANO_STOOL_BLOCK_ENTITY_TYPE;

    public static void register(ModRegistrar registrar) {
        // vanilla 的 BlockEntityType.Builder.of 参数类型不是 public，构造这一步走 loader 钩子
        EXAMPLE_BLOCK_ENTITY_TYPE = register(registrar, "example_block_entity", () ->
                Platform.hooks().createBlockEntityType(ExampleBlockEntity::new, ModBlocks.EXAMPLE_BLOCK.get()));
        JUKEBOX_CONTROLLER_ENTITY_TYPE = register(registrar, "jukebox_controller_block_entity", () ->
                Platform.hooks().createBlockEntityType(JukeboxControllerBlockEntity::new, ModBlocks.JUKEBOX_CONTROLLER.get()));
        NOTE_BLOCK_ENTITY_TYPE = register(registrar, "note_block_entity", () ->
                Platform.hooks().createBlockEntityType(NoteBlockEntity::new, Blocks.NOTE_BLOCK));
        PIANO_BLOCK_ENTITY_TYPE = register(registrar, "piano_block_entity", () ->
                Platform.hooks().createBlockEntityType(PianoBlockEntity::new, ModBlocks.PIANO_BLOCK.get()));
        TONE_BLOCK_ENTITY_TYPE = register(registrar, "tone_block_entity", () ->
                Platform.hooks().createBlockEntityType(ToneBlockEntity::new, ModBlocks.TONE_BLOCK.get()));
        PIANO_STOOL_BLOCK_ENTITY_TYPE = register(registrar, "piano_stool_block_entity", () ->
                Platform.hooks().createBlockEntityType(PianoStoolBlockEntity::new, ModBlocks.PIANO_STOOL_BLOCK.get()));
    }

    public static <T extends BlockEntity> RegistryRef<BlockEntityType<T>> register(ModRegistrar registrar, String name, Supplier<BlockEntityType<T>> supplier) {
        return registrar.register(Registries.BLOCK_ENTITY_TYPE, name, supplier);
    }

    private ModBlockEntityTypes() {
    }
}
