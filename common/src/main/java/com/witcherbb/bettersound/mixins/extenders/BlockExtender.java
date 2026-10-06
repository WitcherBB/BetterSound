package com.witcherbb.bettersound.mixins.extenders;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * <p>实现该接口并实现{@link BlockExtender#getStateFactory()}方法即可自定义{@link BlockState}</p>
 * <p>示例</p>
 * <blockquote><pre>
 *  class ExampleBlockState extends BlockState {
 *      public ExampleBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> values, MapCodec<BlockState> propertiesCodec) {
 *          super(block, values, propertiesCodec);
 *      }
 *      ...
 *  }
 *
 *  class ExampleBlock implements BlockExtender {
 *      ...
 *      {@code @Override}
 *      StateDefinition.Factory<Block, BlockState> getStateFactory() {
 *          return ExampleBlockState::new;
 *      }
 *      ...
 *  }
 * </pre></blockquote>
 */
public interface BlockExtender {
    default StateDefinition.Factory<Block, BlockState> getStateFactory() {
        return BlockState::new;
    }
}
