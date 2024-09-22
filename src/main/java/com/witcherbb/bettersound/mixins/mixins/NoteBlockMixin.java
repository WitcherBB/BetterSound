package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.blocks.entity.NoteBlockEntity;
import com.witcherbb.bettersound.items.TunerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin extends Block implements EntityBlock {

	public NoteBlockMixin(Properties pProperties) {
		super(pProperties);
	}

	@Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
	private void use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
		BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
		if (!pLevel.isClientSide && blockEntity instanceof NoteBlockEntity && pPlayer.getItemInHand(pHand).getItem() instanceof TunerItem) {
			NetworkHooks.openScreen((ServerPlayer) pPlayer, (MenuProvider) blockEntity, pPos);
			cir.setReturnValue(InteractionResult.CONSUME);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return new NoteBlockEntity(pPos, pState);
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos) {
		BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      	return blockentity instanceof MenuProvider ? (MenuProvider)blockentity : null;
	}

}
