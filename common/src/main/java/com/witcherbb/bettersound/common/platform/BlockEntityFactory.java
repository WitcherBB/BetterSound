package com.witcherbb.bettersound.common.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 方块实体工厂：等价于 vanilla 的 {@code BlockEntityType.BlockEntitySupplier}，
 * 但那个类型在 vanilla 里不是 public，common 侧只能用自己的接口。
 */
@FunctionalInterface
public interface BlockEntityFactory<T extends BlockEntity> {
    T create(BlockPos pos, BlockState state);
}
