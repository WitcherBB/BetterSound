package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.utils.ShapeUtil;
import com.witcherbb.bettersound.entities.FakeStoolEntity;
import com.witcherbb.bettersound.entities.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PianoStoolBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final VoxelShape N_MODEL;
    protected static final VoxelShape E_MODEL;
    protected static final VoxelShape S_MODEL;
    protected static final VoxelShape W_MODEL;

    static {
        N_MODEL = Shapes.or(
                PianoStoolBlock.box(1.0, 0.0, 3.0, 3.0, 8.0, 5.0),
                PianoStoolBlock.box(13.0, 0.0, 3.0, 15.0, 8.0, 5.0),
                PianoStoolBlock.box(1.0, 0.0, 11.0, 3.0, 8.0, 13.0),
                PianoStoolBlock.box(13.0, 0.0, 11.0, 15.0, 8.0, 13.0),
                PianoStoolBlock.box(1.0, 8.0, 3.0, 15.0, 12.0, 13.0)
        );

        VoxelShape[] shapes =ShapeUtil.getESWShapes(N_MODEL);
        E_MODEL = shapes[0];
        S_MODEL = shapes[1];
        W_MODEL = shapes[2];
    }

    protected PianoStoolBlock() {
        super(BlockBehaviour.Properties.copy(ModBlocks.PIANO_BLOCK.get()));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            if (!pPlayer.isSecondaryUseActive()) {
                FakeStoolEntity fakeStool = ModEntities.FAKE_STOOL_ENTITY.get().spawn((ServerLevel) pLevel, null, entity -> {
                    entity.setYRot(pState.getValue(FACING).toYRot());
                    entity.setStool(pState);
                    entity.setStoolPos(pPos);
                }, pPos, MobSpawnType.EVENT, false, false);
                if (fakeStool != null) {
                    pPlayer.setYRot(fakeStool.getYRot());
                    pPlayer.setXRot(0.0F);
                    return pPlayer.startRiding(fakeStool) ? InteractionResult.sidedSuccess(pLevel.isClientSide) : InteractionResult.PASS;
                }
            }
        } else {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
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
