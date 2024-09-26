package com.witcherbb.bettersound.data;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.blocks.state.properties.PianoPart;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    private final ModelFile PIANO_PEDAL;
    private final ModelFile PIANO_PEDAL_L;
    private final ModelFile PIANO_PEDAL_R;
    private final ModelFile PIANO_KEYBOARD_L;
    private final ModelFile PIANO_KEYBOARD_M;
    private final ModelFile PIANO_KEYBOARD_R;

    private final ExistingFileHelper existingHelper;
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BetterSound.MODID, exFileHelper);
        this.existingHelper = exFileHelper;
        this.PIANO_PEDAL = new ModelFile.ExistingModelFile(new ResourceLocation(BetterSound.MODID, "block/piano_pedal"), exFileHelper);
        this.PIANO_PEDAL_L = new ModelFile.ExistingModelFile(new ResourceLocation(BetterSound.MODID, "block/piano_pedal_left"), exFileHelper);
        this.PIANO_PEDAL_R = new ModelFile.ExistingModelFile(new ResourceLocation(BetterSound.MODID, "block/piano_pedal_right"), exFileHelper);
        this.PIANO_KEYBOARD_L = new ModelFile.ExistingModelFile(new ResourceLocation(BetterSound.MODID, "block/piano_keyboard_left"), exFileHelper);
        this.PIANO_KEYBOARD_M = new ModelFile.ExistingModelFile(new ResourceLocation(BetterSound.MODID, "block/piano_keyboard_middle"), exFileHelper);
        this.PIANO_KEYBOARD_R = new ModelFile.ExistingModelFile(new ResourceLocation(BetterSound.MODID, "block/piano_keyboard_right"), exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        getVariantBuilder(ModBlocks.PIANO_BLOCK.get()).forAllStatesExcept(state -> {
            PianoPart part = state.getValue(PianoBlock.PART);
            Direction facing = state.getValue(PianoBlock.FACING);

            return ConfiguredModel.builder().modelFile(
                switch (part) {
                    case PEDAL -> PIANO_PEDAL;
                    case PEDAL_R -> PIANO_PEDAL_R;
                    case KEYBOARD_R -> PIANO_KEYBOARD_R;
                    case KEYBOARD_M -> PIANO_KEYBOARD_M;
                    case KEYBOARD_L -> PIANO_KEYBOARD_L;
                    case PEDAL_L -> PIANO_PEDAL_L;
                }
            ).rotationY(switch (facing) {
                case SOUTH -> 180;
                case WEST -> 270;
                case EAST -> 90;
                default -> 0;
            }).build();

        }, PianoBlock.TONE, PianoBlock.POWERED);
    }
}
