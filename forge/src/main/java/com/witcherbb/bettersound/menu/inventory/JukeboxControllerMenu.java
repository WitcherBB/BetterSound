package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;
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
import org.jetbrains.annotations.NotNull;

public class JukeboxControllerMenu extends AbstractContainerMenu {
	private final Level level;
	private final JukeboxControllerBlockEntity blockEntity;

	public JukeboxControllerMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
		this(pContainerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), null);
	}

	public JukeboxControllerMenu(int pContainerId, Inventory inventory, BlockEntity entity, ContainerData data) {
		super(ModMenuTypes.JUKEBOX_CONTROLLER_MENU.get(), pContainerId);
		this.level = inventory.player.level();
		blockEntity = (JukeboxControllerBlockEntity) entity;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return stillValid(ContainerLevelAccess.create(this.level, blockEntity.getBlockPos()),
				player, ModBlocks.JUKEBOX_CONTROLLER.get());
	}

	public JukeboxControllerBlockEntity getBlockEntity() {
		return blockEntity;
	}
}
