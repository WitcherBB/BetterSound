package com.witcherbb.bettersound.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * 调音器。
 *
 * <p>原来重写过 {@code initializeClient} 提供 {@code IClientItemExtensions}，
 * 但实现只是调用 {@code super}（没有任何实际行为），迁移到 common 时直接删掉了——
 * 这样这个类不再依赖 Forge 的客户端扩展 API。
 */
public class TunerItem extends Item {

	protected  TunerItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public boolean canAttackBlock(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, Player pPlayer) {
		return !pPlayer.isCreative() || !(pLevel.getBlockState(pPos).getBlock() instanceof NoteBlock);
	}
}
