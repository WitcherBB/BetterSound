package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.ExampleBlockEntity;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.blocks.extensions.CombinedBlock;
import com.witcherbb.bettersound.blocks.state.properties.BlockPart;
import com.witcherbb.bettersound.blocks.state.properties.ExamplePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ExampleBlock extends BaseEntityBlock implements CombinedBlock<ExamplePart> {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<ExamplePart> PART = EnumProperty.create("example_part", ExamplePart.class);

	public ExampleBlock() {
		super(Properties.copy(Blocks.STONE));
		this.registerDefaultState(getStateDefinition().any().setValue(PART, ExamplePart.FIRST));
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
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
		if (pDirection == this.getCombinedDirection(pState.getValue(PART), pState.getValue(FACING))) {
			return pNeighborState.is(this) && pNeighborState.getValue(PART) != pState.getValue(PART) ? pState : Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		if (!pLevel.isClientSide) {
			BlockPos pos = pPos.relative(this.getCombinedDirection(pState.getValue(PART), pState.getValue(FACING)));
			pLevel.setBlock(pos, pState.setValue(PART, ExamplePart.SECOND), ExampleBlock.UPDATE_ALL);
			pLevel.blockUpdated(pPos, Blocks.AIR);
			pState.updateNeighbourShapes(pLevel, pPos, ExampleBlock.UPDATE_ALL);
		}
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Direction facing = pContext.getHorizontalDirection().getOpposite();
		BlockPos thisPos = pContext.getClickedPos();
		BlockPos combinedPos = thisPos.relative(facing.getCounterClockWise());
		Level level = pContext.getLevel();

		return level.getBlockState(combinedPos).canBeReplaced() && level.getWorldBorder().isWithinBounds(combinedPos) ? this.defaultBlockState().setValue(FACING, facing) : null;
	}

	@Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
		return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING, PART);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : TickableBlockEntity.createTicker();
	}

	@Override
	public Direction getCombinedDirection(ExamplePart part, Direction facing) {
		return switch (part) {
			case FIRST -> facing.getCounterClockWise();
            case SECOND -> facing.getClockWise();
        };
	}
}
