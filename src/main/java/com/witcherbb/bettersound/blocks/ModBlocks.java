package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.BetterSound;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BetterSound.MODID);

    public static final RegistryObject<Block> JUKEBOX_CONTROLLER = BLOCKS.register("jukebox_controller", JukeboxControllerBlock::new);
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", ExampleBlock::new);
    public static final RegistryObject<Block> PIANO_BLOCK = BLOCKS.register("piano_block", () ->
            new PianoBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(0.8F).ignitedByLava()));
    public static final RegistryObject<Block> TONE_BLOCK = BLOCKS.register("tone_block", () ->
            new ToneBlock(BlockBehaviour.Properties.copy(PIANO_BLOCK.get())));
    public static final RegistryObject<Block> CONFINE_PEDAL = BLOCKS.register("confine_pedal", () ->
            new ConfinePedalBlock(BlockBehaviour.Properties.copy(PIANO_BLOCK.get())));
    public static final RegistryObject<Block> PIANO_STOOL_BLOCK = BLOCKS.register("piano_stool_block", () ->
            new PianoStoolBlock(BlockBehaviour.Properties.copy(PIANO_BLOCK.get())));

}
