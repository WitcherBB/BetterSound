package com.witcherbb.bettersound.menu.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlatformContainerMenu extends AbstractContainerMenu {
    protected int slotCount;

    protected AbstractPlatformContainerMenu(@Nullable MenuType<?> menuType, int containerId, int slotCount) {
        super(menuType, containerId);
        this.slotCount = slotCount;
    }

    @Override
    public final ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();
            if (index < 36) {
                if (!canQuickMove(itemStack1, player, index)) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(itemStack1, 36, 36 + slotCount, false)) {
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

    protected final void addPlayerHotBar(Inventory inventory, int x, int y) {
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(inventory, i, x + i * 18, y));
    }

    protected final void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int i = 0; i < 3; i++) {
            for (int l = 0; l < 9; l++) {
                this.addSlot(new Slot(inventory, l + i * 9 + 9, x + l * 18, y + i * 18));
            }
        }
    }

    protected boolean canQuickMove(ItemStack itemStack, Player player, int srcIdx) {
        return false;
    }
}
