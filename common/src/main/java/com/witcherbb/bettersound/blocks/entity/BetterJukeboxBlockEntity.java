package com.witcherbb.bettersound.blocks.entity;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;

public class BetterJukeboxBlockEntity extends BlockEntity implements ContainerSingleItem {
    @NotNull ItemStack recordItemStack = ItemStack.EMPTY;

    private int ticksSinceLastEvent;
    private Boolean isPlaying;
    private long tickCount;
    private long recordStartedTick;

    public BetterJukeboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BETTER_JUKEBOX_BLOCK_ENTITY_TYPE.get(), pos, blockState);
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("RecordItem", 10)) {
            this.setFirstItem(ItemStack.of(tag.getCompound("RecordItem")));
        }

        this.isPlaying = tag.getBoolean("IsPlaying");
        this.recordStartedTick = tag.getLong("RecordStartTick");
        this.tickCount = tag.getLong("TickCount");
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.getFirstItem().isEmpty()) {
            tag.put("RecordItem", this.getFirstItem().save(new CompoundTag()));
        }

        tag.putBoolean("IsPlaying", this.isPlaying);
        tag.putLong("RecordStartTick", this.recordStartedTick);
        tag.putLong("TickCount", this.tickCount);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        if (this.isRecordPlaying()) {
            if (this.getFirstItem().getItem() instanceof RecordItem recordItem) {
                
            }
        }

        ++this.tickCount;
    }

    public Boolean isRecordPlaying() {
        return !this.getFirstItem().isEmpty() && this.isPlaying;
    }

    private Boolean shouldRecordStopPlaying(RecordItem record) {
        return this.tickCount >= this.recordStartedTick + record.getLengthInTicks() + 20;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? recordItemStack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removedStack = ItemStack.EMPTY;
        if (slot == 0 && !recordItemStack.isEmpty()) {
            removedStack = recordItemStack;
            recordItemStack = ItemStack.EMPTY;
        }
        return removedStack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            recordItemStack = stack;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}
