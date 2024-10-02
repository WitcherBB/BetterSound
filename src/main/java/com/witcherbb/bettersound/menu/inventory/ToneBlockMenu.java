package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ToneBlockMenu extends AbstractContainerMenu {
    private final Level level;
    private final ToneBlockEntity toneBlockEntity;

    public ToneBlockMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(pContainerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), null);
    }

    public ToneBlockMenu(int pContainerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TONE_BLOCK_MENU.get(), pContainerId);
        this.level = inventory.player.level();
        this.toneBlockEntity = (ToneBlockEntity) entity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, toneBlockEntity.getBlockPos()),
                pPlayer, ModBlocks.TONE_BLOCK.get());
    }

    public Level getLevel() {
        return level;
    }

    public ToneBlockEntity getToneBlockEntity() {
        return toneBlockEntity;
    }
}
