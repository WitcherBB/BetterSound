package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.ToneBlock;
import com.witcherbb.bettersound.menu.inventory.ToneBlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToneBlockEntity extends AbstractPianoBlockEntity {
    private String toneName = "";

    public ToneBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.TONE_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
    }

    @Override
    public void tick() {
        super.tick();
        this.toneName = toneNameMap.get(this.getBlockState().getValue(ToneBlock.TONE));
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new ToneBlockMenu(pContainerId, pPlayerInventory, this, null);
    }

    public void setToneName(String toneName) {
        this.toneName = toneName;
    }

    public String getToneName() {
        return toneName;
    }
}
