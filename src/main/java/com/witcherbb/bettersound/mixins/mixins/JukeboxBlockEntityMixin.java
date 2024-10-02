package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.menu.inventory.JukeboxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements Clearable, ContainerSingleItem, MenuProvider {

	@Unique
	private final JukeboxBlockEntity betterSound$INSTANCE = (JukeboxBlockEntity) (Object) this;

	@Shadow @Final private NonNullList<ItemStack> items;

	@Shadow public abstract void stopPlaying();

	@Shadow protected abstract void setHasRecordBlockState(@Nullable Entity pEntity, boolean pHasRecord);

	@Shadow private long tickCount;

	@Unique
	private final ItemStackHandler betterSound$itemHandler = new ItemStackHandler(1){
		@Override
		protected void onContentsChanged(int slot) {
			JukeboxBlockEntityMixin.this.items.set(0, this.getStackInSlot(0));
			JukeboxBlockEntityMixin.this.setChanged();
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return slot == betterSound$SLOT1 && stack.is(ItemTags.MUSIC_DISCS);
		}
	};

	@Unique
	private static final int betterSound$SLOT1 = 0;

	@Unique
	private LazyOptional<IItemHandler> betterSound$lazyItemHnadler = LazyOptional.empty();

	@Unique
	protected ContainerData betterSound$data;

	@Unique
	private String betterSound$name = "";

	public JukeboxBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void injected(BlockPos pPos, BlockState pBlockState, CallbackInfo ci) {
		this.betterSound$data = new SimpleContainerData(1) {
			@Override
			public int get(int pIndex) {
				return 0;
			}

			@Override
			public void set(int pIndex, int pValue) {

			}

			@Override
			public int getCount() {
				return 1;
			}
		};
		//betterSound$itemHandler.setStackInSlot(0, getFirstItem());
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER) {
			return betterSound$lazyItemHnadler.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		betterSound$lazyItemHnadler = LazyOptional.of(() -> betterSound$itemHandler);
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", shift =At.Shift.AFTER))
	public void load0(CompoundTag pTag, CallbackInfo ci) {
		this.betterSound$itemHandler.setStackInSlot(betterSound$SLOT1, ItemStack.of(pTag.getCompound("RecordItem")));
	}

	@Inject(method = "load", at = @At("HEAD"))
	public void load1(CompoundTag pTag, CallbackInfo ci) {
		this.betterSound$name = pTag.getString("Name");
	}

	@Inject(method = "saveAdditional", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putBoolean(Ljava/lang/String;Z)V"))
	public void saveAdditional(CompoundTag pTag, CallbackInfo ci) {
		if (!betterSound$itemHandler.getStackInSlot(0).isEmpty())
			pTag.put("RecordItem", betterSound$itemHandler.getStackInSlot(0).save(new CompoundTag()));
		pTag.putString("Name", this.betterSound$name);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/JukeboxBlockEntity;stopPlaying()V"))
	private void tick0(Level pLevel, BlockPos pPos, BlockState pState, CallbackInfo ci) {
		this.setHasRecordBlockState((Entity)null, false);
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void tick1(Level pLevel, BlockPos pPos, BlockState pState, CallbackInfo ci) {
		if (this.getFirstItem().isEmpty()) {
			this.setHasRecordBlockState((Entity)null, false);
			this.stopPlaying();
			++this.tickCount;
			ci.cancel();
		}
	}

	@Inject(method = "removeItem",at = @At(
			value = "HEAD"
	), cancellable = true)
	public void removeItem(int pSlot, int pAmount, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack itemstackOld = this.getFirstItem();
		if (pSlot > 99) {
			if (!itemstackOld.isEmpty()) {
				this.setHasRecordBlockState((Entity)null, false);
				this.stopPlaying();
			}
			cir.setReturnValue(itemstackOld);
		}
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		betterSound$lazyItemHnadler.invalidate();
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("block.minecraft.jukebox");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
		return new JukeboxMenu(pContainerId, pPlayerInventory, (JukeboxBlockEntity) (Object) this, this.betterSound$data);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		saveAdditional(nbt);
		return nbt;
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(betterSound$INSTANCE);
	}

}
