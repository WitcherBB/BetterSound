package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.ExampleBlockEntity;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ExampleBlock extends BaseEntityBlock {
	public ExampleBlock() {
		super(Properties.copy(Blocks.STONE));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new ExampleBlockEntity(pPos, pState);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		BlockEntity entity = pLevel.getBlockEntity(pPos);
		if (!pLevel.isClientSide()){
			if (entity instanceof ExampleBlockEntity exampleBlockEntity) {
				NetworkHooks.openScreen((ServerPlayer) pPlayer, exampleBlockEntity, pPos);
			} else {
				throw new IllegalStateException("Our Container provider is missing");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		if (!pLevel.isClientSide) {
			BlockPos pos = pPos.relative(Direction.NORTH);
			pLevel.setBlock(pos, pState, ExampleBlock.UPDATE_ALL);
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : TickableBlockEntity.createTicker();
	}
}
