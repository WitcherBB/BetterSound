package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.Constants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Constants.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> JUKEBOX_CONTROLLER =
            BLOCKS.register("jukebox_controller", JukeboxControllerBlock::new);
    public static final RegistrySupplier<Block> EXAMPLE_BLOCK =
            BLOCKS.register("example_block", ExampleBlock::new);
    public static final RegistrySupplier<Block> PIANO_BLOCK =
            BLOCKS.register("piano_block", PianoBlock::new);
    public static final RegistrySupplier<Block> TONE_BLOCK =
            BLOCKS.register("tone_block", ToneBlock::new);
    public static final RegistrySupplier<Block> SUSTAIN_PEDAL =
            BLOCKS.register("sustain_pedal", SustainPedalBlock::new);
    public static final RegistrySupplier<Block> PIANO_STOOL_BLOCK =
            BLOCKS.register("piano_stool_block", PianoStoolBlock::new);
    public static final RegistrySupplier<Block> BETTER_JUKEBOX_BLOCK =
            BLOCKS.register("better_jukebox", BetterJukeboxBlock::new);


    public static void register() {
        BLOCKS.register();
    }

    private ModBlocks() {
    }
}
