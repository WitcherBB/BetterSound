package com.witcherbb.bettersound.blocks.entity.utils;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {
	void tick();

	static <T extends BlockEntity> BlockEntityTicker<T> createTicker() {
		return (pLevel, pPos, pState, pBlockEntity) -> {
			try {
				((TickableBlockEntity) pBlockEntity).tick();
			} catch (Exception ignored) {
			}
		};
	}
}
