package com.witcherbb.bettersound.blocks.extensions;

import com.witcherbb.bettersound.blocks.state.properties.BlockPart;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 建议添加朝向。</br>
 * 参见 {@link net.minecraft.world.level.block.state.properties.BlockStateProperties#HORIZONTAL_FACING} 或者
 * {@link net.minecraft.world.level.block.state.properties.BlockStateProperties#FACING}
 */
public interface CombinedBlock<P extends BlockPart> {
    Direction getCombinedDirection(P part, Direction baseDirection);
}
