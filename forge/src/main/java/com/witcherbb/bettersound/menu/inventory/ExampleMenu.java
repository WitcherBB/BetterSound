package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.entity.ExampleBlockEntity;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExampleMenu extends AbstractContainerMenu {
	private final Level level;
	private final ExampleBlockEntity blockEntity;

	public ExampleMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
		this(pContainerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), null);
	}

	public ExampleMenu(int pContainerId, Inventory inventory, BlockEntity entity, ContainerData data) {
		super(ModMenuTypes.EXAMPLE_MENU.get(), pContainerId);
		level = inventory.player.level();
		blockEntity = (ExampleBlockEntity) entity;
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return null;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
				pPlayer, ModBlocks.EXAMPLE_BLOCK.get());
	}

	public ExampleBlockEntity getBlockEntity() {
		return blockEntity;
	}
}
