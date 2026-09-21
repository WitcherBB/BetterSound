package com.witcherbb.bettersound.menu.inventory;

import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ToneBlockMenu extends AbstractPianoMenu {
    public ToneBlockMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(pContainerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), null);
    }

    public ToneBlockMenu(int pContainerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TONE_BLOCK_MENU.get(), pContainerId, inventory, entity, data);
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, pianoBlockEntity.getBlockPos()),
                pPlayer, ModBlocks.TONE_BLOCK.get());
    }
}
