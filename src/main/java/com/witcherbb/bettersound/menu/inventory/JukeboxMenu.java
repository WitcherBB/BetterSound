package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class JukeboxMenu extends AbstractContainerMenu {
	public final JukeboxBlockEntity blockEntity;
	private final Level level;
	private final ContainerData data;
	private SlotItemHandler slotItemHandler;
	private String name;

	public JukeboxMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
		this(pContainerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
	}

	public JukeboxMenu(int pContainerId, Inventory inventory, BlockEntity entity, ContainerData data) {
		super(ModMenuTypes.JUKEBOX_MENU.get(), pContainerId);
		checkContainerSize(inventory, 1);
		blockEntity = (JukeboxBlockEntity) entity;
		this.level = inventory.player.level();
		this.data = data;

		addPlayerInventory(inventory);
		addPlayerHotBar(inventory);

		this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler ->
				this.slotItemHandler = (SlotItemHandler) this.addSlot(new SlotItemHandler(iItemHandler, 0, 80, 35) {
					@Override
					public boolean mayPlace(@NotNull ItemStack stack) {
						return super.mayPlace(stack);
					}
        }));

//		addDataSlots(data);
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(pIndex);

		if (slot.hasItem()) {
			ItemStack itemStack1 = slot.getItem();
			itemStack = itemStack1.copy();
			if (pIndex < 36) {
				if (!itemStack1.is(ItemTags.MUSIC_DISCS)) {
					return ItemStack.EMPTY;
				}
				if (!this.moveItemStackTo(itemStack1, 36, 37, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (!this.moveItemStackTo(itemStack1, 0, 36, true)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemStack1.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemStack;
	}

	@Override
	public boolean stillValid(@NotNull Player pPlayer) {
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
				pPlayer, Blocks.JUKEBOX);
	}

	private void addPlayerHotBar(Inventory inventory) {
		for (int i = 0; i < 9; i++)
			this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
	}

	private void addPlayerInventory(Inventory inventory) {
		for (int i = 0; i < 3; i++) {
			for (int l = 0; l < 9; l++) {
				this.addSlot(new Slot(inventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
			}
		}
	}

	public JukeboxBlockEntity getBlockEntity() {
		return blockEntity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
