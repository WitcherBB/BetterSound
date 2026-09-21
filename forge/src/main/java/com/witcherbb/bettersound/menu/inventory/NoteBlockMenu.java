package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.blocks.entity.NoteBlockEntity;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class NoteBlockMenu extends AbstractContainerMenu {
	private Level level;
	private NoteBlockEntity blockEntity;

	public NoteBlockMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
		this(pContainerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
	}

	public NoteBlockMenu(int pContainerId, Inventory inventory, BlockEntity entity) {
		super(ModMenuTypes.NOTE_BLOCK_MENU.get(), pContainerId);
		level = inventory.player.level();
		blockEntity = (NoteBlockEntity) entity;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(@NotNull Player pPlayer) {
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
				pPlayer, Blocks.NOTE_BLOCK);
	}

	public Level level() {
		return level;
	}

	public NoteBlockEntity getBlockEntity() {
		return blockEntity;
	}
}
