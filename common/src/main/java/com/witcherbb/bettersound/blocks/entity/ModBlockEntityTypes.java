package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.platform.BlockEntityFactory;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Arrays;
import java.util.function.Supplier;

/** 方块实体类型登记表。 */
public final class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Constants.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<ExampleBlockEntity>> EXAMPLE_BLOCK_ENTITY_TYPE =
            register("example_block_entity", ExampleBlockEntity::new, ModBlocks.EXAMPLE_BLOCK);
    public static final RegistrySupplier<BlockEntityType<JukeboxControllerBlockEntity>> JUKEBOX_CONTROLLER_ENTITY_TYPE =
            register("jukebox_controller_block_entity", JukeboxControllerBlockEntity::new,
                    ModBlocks.JUKEBOX_CONTROLLER);
    public static final RegistrySupplier<BlockEntityType<NoteBlockEntity>> NOTE_BLOCK_ENTITY_TYPE =
            register("note_block_entity", NoteBlockEntity::new, () -> Blocks.NOTE_BLOCK);
    public static final RegistrySupplier<BlockEntityType<PianoBlockEntity>> PIANO_BLOCK_ENTITY_TYPE =
            register("piano_block_entity", PianoBlockEntity::new, ModBlocks.PIANO_BLOCK);
    public static final RegistrySupplier<BlockEntityType<ToneBlockEntity>> TONE_BLOCK_ENTITY_TYPE =
            register("tone_block_entity", ToneBlockEntity::new, ModBlocks.TONE_BLOCK);
    public static final RegistrySupplier<BlockEntityType<PianoStoolBlockEntity>> PIANO_STOOL_BLOCK_ENTITY_TYPE =
            register("piano_stool_block_entity", PianoStoolBlockEntity::new, ModBlocks.PIANO_STOOL_BLOCK);
    public static final RegistrySupplier<BlockEntityType<BetterJukeboxBlockEntity>> BETTER_JUKEBOX_BLOCK_ENTITY_TYPE =
            register("better_jukebox_block_entity", BetterJukeboxBlockEntity::new , ModBlocks.BETTER_JUKEBOX_BLOCK);

    public static void register() {
        BLOCK_ENTITY_TYPES.register();
    }

    @SafeVarargs
    public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String name, BlockEntityFactory<T> factory, Supplier<Block>... blocks) {

        return BLOCK_ENTITY_TYPES.register(name, () -> {
            Block[] resolved = Arrays.stream(blocks)
                    .map(Supplier::get)
                    .toArray(Block[]::new);
            return BlockEntityType.Builder.of(factory::create, resolved).build(null);
        });
    }

    private ModBlockEntityTypes() {
    }
}
