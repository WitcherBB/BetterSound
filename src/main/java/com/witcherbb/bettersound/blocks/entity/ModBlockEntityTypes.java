package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BetterSound.MODID);

	public static final RegistryObject<BlockEntityType<ExampleBlockEntity>> EXAMPLE_BLOCK_ENTITY_TYPE =
			register("example_block_entity", () ->
					BlockEntityType.Builder.of(ExampleBlockEntity::new, ModBlocks.EXAMPLE_BLOCK.get())
							.build(null));
	public static final RegistryObject<BlockEntityType<JukeboxControllerBlockEntity>> JUKEBOX_CONTROLLER_ENTITY_TYPE =
			register("jukebox_controller_block_entity", () ->
					BlockEntityType.Builder.of(JukeboxControllerBlockEntity::new, ModBlocks.JUKEBOX_CONTROLLER.get())
							.build(null));
	public static final RegistryObject<BlockEntityType<NoteBlockEntity>> NOTE_BLOCK_ENTITY_TYPE =
			register("note_block_entity", () ->
					BlockEntityType.Builder.of(NoteBlockEntity::new, Blocks.NOTE_BLOCK)
							.build(null));

	public static final RegistryObject<BlockEntityType<PianoBlockEntity>> PIANO_BLOCK_ENTITY_TYPE =
			register("piano_block_entity", () ->
					BlockEntityType.Builder.of(PianoBlockEntity::new, ModBlocks.PIANO_BLOCK.get())
							.build(null));

	public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, Supplier<BlockEntityType<T>> sup) {
		return BLOCK_ENTITIES.register(name, sup);
	}
}
