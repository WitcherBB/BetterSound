package com.witcherbb.bettersound.fabric.data;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.blocks.PianoStoolBlock;
import com.witcherbb.bettersound.blocks.state.properties.PianoPart;
import com.witcherbb.bettersound.items.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

final class ModelProvider extends FabricModelProvider {
    private static final TextureMapping RECORD_TEXTURE_MAPPING = new TextureMapping()
            .put(TextureSlot.LAYER0, new ResourceLocation(Constants.MOD_ID, "item/sdut_music_disc"));

    private final ResourceLocation PIANO_PEDAL;
    private final ResourceLocation PIANO_PEDAL_DOWN;
    private final ResourceLocation PIANO_PEDAL_L;
    private final ResourceLocation PIANO_PEDAL_R;
    private final ResourceLocation PIANO_KEYBOARD_L;
    private final ResourceLocation PIANO_KEYBOARD_M;
    private final ResourceLocation PIANO_KEYBOARD_R;

    public ModelProvider(FabricDataOutput output) {
        super(output);
        this.PIANO_PEDAL = new ResourceLocation(Constants.MOD_ID, "block/piano_pedal");
        this.PIANO_PEDAL_DOWN = new ResourceLocation(Constants.MOD_ID, "block/piano_pedal_down");
        this.PIANO_PEDAL_L = new ResourceLocation(Constants.MOD_ID, "block/piano_pedal_left");
        this.PIANO_PEDAL_R = new ResourceLocation(Constants.MOD_ID, "block/piano_pedal_right");
        this.PIANO_KEYBOARD_L = new ResourceLocation(Constants.MOD_ID, "block/piano_keyboard_left");
        this.PIANO_KEYBOARD_M = new ResourceLocation(Constants.MOD_ID, "block/piano_keyboard_middle");
        this.PIANO_KEYBOARD_R = new ResourceLocation(Constants.MOD_ID, "block/piano_keyboard_right");
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators gen) {
        generatePianoBlockState(gen);
        generatePianoStoolBlockState(gen);
    }

    @Override
    public void generateItemModels(ItemModelGenerators gen) {
        generateRecordItemModel(gen);
    }

    private void generatePianoBlockState(BlockModelGenerators gen) {
        Block pianoBlock = ModBlocks.PIANO_BLOCK.get();
        var dispatch = PropertyDispatch.properties(PianoBlock.PART, PianoBlock.FACING, PianoBlock.DELAY);
        for (PianoPart part : PianoBlock.PART.getPossibleValues()) {
            for (Direction direction : PianoBlock.FACING.getPossibleValues()) {
                for (Boolean delay : PianoBlock.DELAY.getPossibleValues()) {
                    Variant variant = Variant.variant()
                            .with(VariantProperties.MODEL, switch (part) {
                                case PEDAL -> delay ? PIANO_PEDAL_DOWN : PIANO_PEDAL;
                                case PEDAL_R -> PIANO_PEDAL_R;
                                case KEYBOARD_R -> PIANO_KEYBOARD_R;
                                case KEYBOARD_M -> PIANO_KEYBOARD_M;
                                case KEYBOARD_L -> PIANO_KEYBOARD_L;
                                case PEDAL_L -> PIANO_PEDAL_L;
                            }).with(VariantProperties.Y_ROT, switch (direction) {
                                case SOUTH -> VariantProperties.Rotation.R180;
                                case WEST -> VariantProperties.Rotation.R270;
                                case EAST -> VariantProperties.Rotation.R90;
                                default -> VariantProperties.Rotation.R0;
                            });
                    dispatch.select(part, direction, delay, variant);
                }
            }
        }
        gen.blockStateOutput.accept(MultiVariantGenerator.multiVariant(pianoBlock).with(dispatch));
        gen.skipAutoItemBlock(pianoBlock);
    }

    private void generatePianoStoolBlockState(BlockModelGenerators gen) {
        Block pianoStoolBlock = ModBlocks.PIANO_STOOL_BLOCK.get();
        var dispatch = PropertyDispatch.property(PianoStoolBlock.FACING);
        for (Direction facing : PianoStoolBlock.FACING.getPossibleValues()) {
            dispatch.select(facing, Variant.variant()
                    .with(VariantProperties.MODEL, new ResourceLocation(Constants.MOD_ID, "block/" + ModBlocks.PIANO_STOOL_BLOCK.getId().getPath()))
                    .with(VariantProperties.Y_ROT, switch (facing) {
                        case SOUTH -> VariantProperties.Rotation.R180;
                        case WEST -> VariantProperties.Rotation.R270;
                        case EAST -> VariantProperties.Rotation.R90;
                        default -> VariantProperties.Rotation.R0;
                    }));
        }
        gen.blockStateOutput.accept(MultiVariantGenerator.multiVariant(pianoStoolBlock).with(dispatch));
        gen.skipAutoItemBlock(pianoStoolBlock);
    }

    private void generateRecordItemModel(ItemModelGenerators gen) {
        for (var itemSupplier : ModItems.music_discs_list) {
            ModelTemplates.FLAT_ITEM.create(new ResourceLocation(Constants.MOD_ID, "item/" + itemSupplier.getId().getPath()),
                    RECORD_TEXTURE_MAPPING, gen.output);
        }
    }
}
