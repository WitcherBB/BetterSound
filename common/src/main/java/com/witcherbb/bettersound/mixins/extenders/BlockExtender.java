package com.witcherbb.bettersound.mixins.extenders;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public interface BlockExtender {
    default StateDefinition.Factory<Block, BlockState> getStateFactory() {
        return BlockState::new;
    }
}
