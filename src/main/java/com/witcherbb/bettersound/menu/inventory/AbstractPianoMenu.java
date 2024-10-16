package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.blocks.entity.AbstractPianoBlockEntity;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPianoMenu extends AbstractContainerMenu {
    protected final Level level;
    protected final AbstractPianoBlockEntity pianoBlockEntity;

    public AbstractPianoMenu(MenuType<?> pType, int pContainerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(pType, pContainerId);
        this.level = inventory.player.level();
        this.pianoBlockEntity = (AbstractPianoBlockEntity) entity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    public Level getLevel() {
        return level;
    }

    public AbstractPianoBlockEntity getPianoBlockEntity() {
        return pianoBlockEntity;
    }
}
