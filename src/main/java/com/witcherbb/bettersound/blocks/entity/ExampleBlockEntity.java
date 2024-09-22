package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.menu.inventory.ExampleMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExampleBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {

	private String name = "";

	public ExampleBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(ModBlockEntityTypes.EXAMPLE_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		this.name = nbt.getString("Name");
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putString("Name", this.name);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("block.sdutmod.example_block");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, Player pPlayer) {
		return new ExampleMenu(pContainerId, pPlayerInventory, this, null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void tick() {
		if (this.level == null || this.level.isClientSide())
			return;

		//sync to the client
		this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Name", this.name);
		return nbt;
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
