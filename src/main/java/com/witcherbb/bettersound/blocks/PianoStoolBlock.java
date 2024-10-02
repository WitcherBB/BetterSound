package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.PianoStoolBlockEntity;
import com.witcherbb.bettersound.blocks.utils.ShapeUtil;
import com.witcherbb.bettersound.mixins.extenders.PlayerSittingExtender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PianoStoolBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final VoxelShape N_MODEL;
    protected static final VoxelShape E_MODEL;
    protected static final VoxelShape S_MODEL;
    protected static final VoxelShape W_MODEL;

    static {
        N_MODEL = Shapes.or(
                PianoStoolBlock.box(2.0, 0.0, 3.0, 4.0, 8.0, 5.0),
                PianoStoolBlock.box(12.0, 0.0, 3.0, 14.0, 8.0, 5.0),
                PianoStoolBlock.box(2.0, 0.0, 11.0, 4.0, 8.0, 13.0),
                PianoStoolBlock.box(12.0, 0.0, 11.0, 14.0, 8.0, 13.0),
                PianoStoolBlock.box(2.0, 8.0, 3.0, 14.0, 12.0, 13.0)
        );

        VoxelShape[] shapes =ShapeUtil.getESWShapes(N_MODEL);
        E_MODEL = shapes[0];
        S_MODEL = shapes[1];
        W_MODEL = shapes[2];
    }

    protected PianoStoolBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        PianoStoolBlockEntity blockEntity = (PianoStoolBlockEntity) pLevel.getBlockEntity(pPos);
        if (blockEntity != null && blockEntity.canAddPassenger(pPlayer)) {
            Direction facing = pState.getValue(FACING);
            float yRot = facing.toYRot();
            pPlayer.setYRot(yRot);
            pPlayer.setXRot(0.0F);
            ((PlayerSittingExtender) pPlayer).startSitting(blockEntity);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PianoStoolBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case NORTH -> N_MODEL;
            case SOUTH -> S_MODEL;
            case WEST -> W_MODEL;
            case EAST -> E_MODEL;
            default -> Shapes.block();
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction facing = pContext.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }
}
