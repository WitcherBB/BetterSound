package com.witcherbb.bettersound.blocks.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Tickers {
	public static void jukeboxTicker(Level pLevel, BlockPos pPos, BlockState pState, JukeboxBlockEntity pJukebox) {
		if (pLevel != null)
			pLevel.sendBlockUpdated(pPos, pState, pState, Block.UPDATE_ALL);
	}
}
