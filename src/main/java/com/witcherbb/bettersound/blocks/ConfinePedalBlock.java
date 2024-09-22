package com.witcherbb.bettersound.blocks;

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

public class ConfinePedalBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty STEPED = BooleanProperty.create("steped");

    protected static final VoxelShape N_FRAME_SHAPE = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 6.0D, 16.0D);
    protected static final VoxelShape N_L_PEDAL_SHAPE = Block.box(3.0D, 3.0D, 8.0D, 5.0D, 4.0D, 14.0D);
    protected static final VoxelShape N_M_PEDAL_SHAPE = Block.box(7.0D, 3.0D, 8.0D, 9.0D, 4.0D, 14.0D);
    protected static final VoxelShape N_R_PEDAL_SHAPE = Block.box(11.0D, 3.0D, 8.0D, 13.0D, 4.0D, 14.0D);
    protected static final VoxelShape E_FRAME_SHAPE = rotateShapeY(N_FRAME_SHAPE, 90.0D);
    protected static final VoxelShape E_L_PEDAL_SHAPE = rotateShapeY(N_L_PEDAL_SHAPE, 90.0D);
    protected static final VoxelShape E_M_PEDAL_SHAPE = rotateShapeY(N_M_PEDAL_SHAPE, 90.0D);
    protected static final VoxelShape E_R_PEDAL_SHAPE = rotateShapeY(N_R_PEDAL_SHAPE, 90.0D);
    protected static final VoxelShape S_FRAME_SHAPE = rotateShapeY(N_FRAME_SHAPE, 180.0D);
    protected static final VoxelShape S_L_PEDAL_SHAPE = rotateShapeY(N_L_PEDAL_SHAPE, 180.0D);
    protected static final VoxelShape S_M_PEDAL_SHAPE = rotateShapeY(N_M_PEDAL_SHAPE, 180.0D);
    protected static final VoxelShape S_R_PEDAL_SHAPE = rotateShapeY(N_R_PEDAL_SHAPE, 180.0D);
    protected static final VoxelShape W_FRAME_SHAPE = rotateShapeY(N_FRAME_SHAPE, 270.0D);
    protected static final VoxelShape W_L_PEDAL_SHAPE = rotateShapeY(N_L_PEDAL_SHAPE, 270.0D);
    protected static final VoxelShape W_M_PEDAL_SHAPE = rotateShapeY(N_M_PEDAL_SHAPE, 270.0D);
    protected static final VoxelShape W_R_PEDAL_SHAPE = rotateShapeY(N_R_PEDAL_SHAPE, 270.0D);

    public ConfinePedalBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(getStateDefinition().any().setValue(POWERED, false).setValue(FACING, Direction.NORTH).setValue(STEPED, false));
    }

    protected static VoxelShape rotateShapeY(VoxelShape shape, double angle) {
        int times = (int) Math.floor(angle / 90) % 4;
        double minX = shape.min(Direction.Axis.X);
        double maxX = shape.max(Direction.Axis.X);
        double minY = shape.min(Direction.Axis.Y);
        double maxY = shape.max(Direction.Axis.Y);
        double minZ = shape.min(Direction.Axis.Z);
        double maxZ = shape.max(Direction.Axis.Z);
        double size = 1.0D;

        return switch (times) {
            case 0 -> Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
            case 1 -> Shapes.box(size - maxZ, minY, minX, size - minZ, maxY, maxX);
            case 2 -> Shapes.box(size - maxX, minY, size - maxZ, size - minX, maxY, size - minZ);
            case 3 -> Shapes.box(minZ, minY, size - maxX, maxZ, maxY, size - minX);
            default -> throw new IllegalStateException("Shape Wrong!");
        };
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case NORTH -> Shapes.or(N_FRAME_SHAPE, N_L_PEDAL_SHAPE, N_M_PEDAL_SHAPE, N_R_PEDAL_SHAPE);
            case WEST -> Shapes.or(W_FRAME_SHAPE, W_L_PEDAL_SHAPE, W_M_PEDAL_SHAPE, W_R_PEDAL_SHAPE);
            case EAST -> Shapes.or(E_FRAME_SHAPE, E_L_PEDAL_SHAPE, E_M_PEDAL_SHAPE, E_R_PEDAL_SHAPE);
            case SOUTH -> Shapes.or(S_FRAME_SHAPE, S_L_PEDAL_SHAPE, S_M_PEDAL_SHAPE, S_R_PEDAL_SHAPE);
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
