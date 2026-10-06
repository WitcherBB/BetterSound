package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.blocks.entity.BetterJukeboxBlockEntity;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BetterJukeboxMenu extends AbstractPlatformContainerMenu {
    public BetterJukeboxBlockEntity blockEntity;

    public BetterJukeboxMenu(int containerId, Inventory inventory, @NotNull FriendlyByteBuf buf) {
        this(containerId, inventory, (BetterJukeboxBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public BetterJukeboxMenu(int containerId, Inventory inventory, BetterJukeboxBlockEntity entity) {
        super(ModMenuTypes.BETTER_JUKEBOX_MENU.get(), containerId, 1);
        checkContainerSize(inventory, 1);
        this.blockEntity = entity;

        this.addPlayerInventory(inventory, 8, 84);
        this.addPlayerHotBar(inventory, 8, 142);

        this.addSlot(new Slot(this.blockEntity, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemTags.MUSIC_DISCS);
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    @Override
    protected boolean canQuickMove(ItemStack itemStack, Player player, int srcIdx) {
        return true;
    }

    public BetterJukeboxBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
