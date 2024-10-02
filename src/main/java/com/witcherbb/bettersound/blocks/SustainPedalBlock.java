package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.utils.ShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SustainPedalBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty STEPED = BooleanProperty.create("steped");

    protected static final VoxelShape N_PEDAL_SHAPE;
    protected static final VoxelShape E_PEDAL_SHAPE;
    protected static final VoxelShape S_PEDAL_SHAPE;
    protected static final VoxelShape W_PEDAL_SHAPE;

    static {
        N_PEDAL_SHAPE = Shapes.or(
                Block.box(0.0, 0.0, 15.0, 16.0, 6.0, 16.0),
                Block.box(3.0, 3.0, 9.0, 5.0, 4.0, 15.0),
                Block.box(7.0, 3.0, 9.0, 9.0, 4.0, 15.0),
                Block.box(11.0, 3.0, 9.0, 13.0, 4.0, 15.0)
        );
        VoxelShape[] shapes = ShapeUtil.getESWShapes(N_PEDAL_SHAPE);
        E_PEDAL_SHAPE = shapes[0];
        S_PEDAL_SHAPE = shapes[1];
        W_PEDAL_SHAPE = shapes[2];
    }

    protected SustainPedalBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(getStateDefinition().any().setValue(POWERED, false).setValue(FACING, Direction.NORTH).setValue(STEPED, false));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case NORTH -> N_PEDAL_SHAPE;
            case WEST -> W_PEDAL_SHAPE;
            case EAST -> E_PEDAL_SHAPE;
            case SOUTH -> S_PEDAL_SHAPE;
            default -> Shapes.empty();
        };
    }

    @Override
    public StateDefinition<Block, BlockState> getStateDefinition() {
        return super.getStateDefinition();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockState state = pState.cycle(STEPED);
            pLevel.setBlock(pPos, state, Block.UPDATE_ALL);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        boolean flag = pLevel.hasNeighborSignal(pPos);
        if (!pLevel.isClientSide && flag != pState.getValue(POWERED)) {
            if (flag) {
                pLevel.setBlock(pPos, pState.setValue(POWERED, true).setValue(STEPED, true), Block.UPDATE_ALL);
            } else {
                pLevel.setBlock(pPos, pState.setValue(POWERED, false).setValue(STEPED, false), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(POWERED, FACING, STEPED);
    }
}
