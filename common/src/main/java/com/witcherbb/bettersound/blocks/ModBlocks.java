package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** 方块登记表。与 loader 无关：具体注册由 {@link ModRegistrar} 实现完成。 */
public final class ModBlocks {

    public static RegistryRef<Block> JUKEBOX_CONTROLLER;
    public static RegistryRef<Block> EXAMPLE_BLOCK;
    public static RegistryRef<Block> PIANO_BLOCK;
    public static RegistryRef<Block> TONE_BLOCK;
    public static RegistryRef<Block> SUSTAIN_PEDAL;
    public static RegistryRef<Block> PIANO_STOOL_BLOCK;

    /** 由各 loader 的入口在注册阶段调用一次（顺序有讲究：后面的方块会复制 PIANO_BLOCK 的属性）。 */
    public static void register(ModRegistrar registrar) {
        JUKEBOX_CONTROLLER = registrar.register(Registries.BLOCK, "jukebox_controller", JukeboxControllerBlock::new);
        EXAMPLE_BLOCK = registrar.register(Registries.BLOCK, "example_block", ExampleBlock::new);
        PIANO_BLOCK = registrar.register(Registries.BLOCK, "piano_block", () ->
                new PianoBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(0.8F).ignitedByLava()));
        TONE_BLOCK = registrar.register(Registries.BLOCK, "tone_block", () ->
                new ToneBlock(BlockBehaviour.Properties.copy(PIANO_BLOCK.get())));
        SUSTAIN_PEDAL = registrar.register(Registries.BLOCK, "sustain_pedal", () ->
                new SustainPedalBlock(BlockBehaviour.Properties.copy(PIANO_BLOCK.get())));
        PIANO_STOOL_BLOCK = registrar.register(Registries.BLOCK, "piano_stool_block", () ->
                new PianoStoolBlock(BlockBehaviour.Properties.copy(PIANO_BLOCK.get())));
    }

    private ModBlocks() {
    }
}
