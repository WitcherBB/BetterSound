package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.menu.inventory.NoteBlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NoteBlockEntity extends BlockEntity implements MenuProvider {
	public NoteBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(ModBlockEntityTypes.NOTE_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("block.minecraft.note_block");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		return new NoteBlockMenu(pContainerId, pPlayerInventory, this);
	}

}
