package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.ToneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ToneBlockEntity extends AbstractPianoBlockEntity {
    private String toneName = "";

    public ToneBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.TONE_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public void setToneName(String toneName) {
        this.toneName = toneName;
    }

    public String getToneName() {
        return toneNameMap.get(this.getBlockState().getValue(ToneBlock.TONE));
    }
}
