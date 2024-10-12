package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.blocks.extensions.CombinedBlock;
import com.witcherbb.bettersound.blocks.state.properties.PianoPart;
import com.witcherbb.bettersound.blocks.utils.ShapeUtil;
import com.witcherbb.bettersound.common.utils.Util;
import com.witcherbb.bettersound.mixins.extenders.MinecraftServerExtender;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.CPianoBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.CPianoBlockStopPacket;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PianoBlock extends BaseEntityBlock implements CombinedBlock<PianoPart> {
    public static final IntegerProperty TONE = IntegerProperty.create("tones", 0, 87);
    public static final EnumProperty<PianoPart> PART = EnumProperty.create("part", PianoPart.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty DELAY = BooleanProperty.create("delay");
    private static final PianoPart LAST_PART = PianoPart.PEDAL_L;

    protected static final VoxelShape N_PEDAL_SHAPE;
    protected static final VoxelShape N_PEDAL_L_SHAPE;
    protected static final VoxelShape N_PEDAL_R_SHAPE;
    protected static final VoxelShape N_KEYBOARD_L_SHAPE;
    protected static final VoxelShape N_KEYBOARD_M_SHAPE;
    protected static final VoxelShape N_KEYBOARD_R_SHAPE;
    protected static final VoxelShape W_PEDAL_SHAPE;
    protected static final VoxelShape W_PEDAL_L_SHAPE;
    protected static final VoxelShape W_PEDAL_R_SHAPE;
    protected static final VoxelShape W_KEYBOARD_L_SHAPE;
    protected static final VoxelShape W_KEYBOARD_M_SHAPE;
    protected static final VoxelShape W_KEYBOARD_R_SHAPE;
    protected static final VoxelShape S_PEDAL_SHAPE;
    protected static final VoxelShape S_PEDAL_L_SHAPE;
    protected static final VoxelShape S_PEDAL_R_SHAPE;
    protected static final VoxelShape S_KEYBOARD_L_SHAPE;
    protected static final VoxelShape S_KEYBOARD_M_SHAPE;
    protected static final VoxelShape S_KEYBOARD_R_SHAPE;
    protected static final VoxelShape E_PEDAL_SHAPE;
    protected static final VoxelShape E_PEDAL_L_SHAPE;
    protected static final VoxelShape E_PEDAL_R_SHAPE;
    protected static final VoxelShape E_KEYBOARD_L_SHAPE;
    protected static final VoxelShape E_KEYBOARD_M_SHAPE;
    protected static final VoxelShape E_KEYBOARD_R_SHAPE;

    static {
        N_PEDAL_L_SHAPE = Shapes.or(
                Block.box(15.0, 0.0, 0.0, 16.0, 15.0, 16.0),
                Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
                Block.box(0.0, 11.0, 9.0, 15.0, 12.0, 10.0),
                Block.box(4.0, 3.0, 9.0, 6.0, 11.0, 10.0),
                Block.box(0.0, 1.0, 9.0, 15.0, 3.0, 10.0)
        );
        N_PEDAL_SHAPE = Shapes.or(
                Block.box(0.0, 1.0, 9.0, 16.0, 3.0, 10.0),
                Block.box(1.0, 3.0, 9.0, 15.0, 11.0, 10.0),
                Block.box(0.0, 11.0, 9.0, 16.0, 12.0, 10.0),
                Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
                Block.box(11.0, 2.0, 5.0, 13.0, 3.0, 9.0),
                Block.box(7.0, 2.0, 5.0, 9.0, 3.0, 9.0),
                Block.box(3.0, 2.0, 5.0, 5.0, 3.0, 9.0)
        );
        N_PEDAL_R_SHAPE = Shapes.or(
                Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
                Block.box(0.0, 0.0, 0.0, 1.0, 15.0,16.0),
                Block.box(1.0, 1.0, 9.0, 16.0, 3.0, 10.0),
                Block.box(10.0, 3.0, 9.0, 12.0, 11.0, 10.0),
                Block.box(1.0, 11.0, 9.0, 16.0, 12.0, 10.0)
        );
        N_KEYBOARD_L_SHAPE = Shapes.or(
                Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
                Block.box(15.0, 1.0, 0.0, 16.0, 4.0, 8.0),
                Block.box(0.0, 1.0, 8.0, 15.0, 3.0, 9.0),
                Block.box(15.0, 1.0, 8.0, 16.0, 8.0, 16.0),
                Block.box(0.0, 1.0, 9.0, 16.0, 8.0, 16.0),
                Block.box(0.0, 8.0, 7.0, 16.0, 9.0, 16.0),
                Block.box(0.0, 1.0, 1.0, 15.0, 2.0, 8.0)
        );
        N_KEYBOARD_M_SHAPE = Shapes.or(
                Block.box(0.0 ,0.0, 0.0, 16.0, 1.0, 16.0),
                Block.box(0.0 ,1.0, 8.0, 16.0, 3.0, 9.0),
                Block.box(0.0 ,1.0, 9.0, 16.0, 8.0, 16.0),
                Block.box(0.0, 8.0, 7.0, 16.0, 9.0, 16.0),
                Block.box(2.0, 8.0, 11.0, 14.0, 10.0, 13.0),
                Block.box(0.0, 1.0, 1.0, 16.0, 2.0, 8.0)
        );
        N_KEYBOARD_R_SHAPE = Shapes.or(
                Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
                Block.box(0.0, 1.0, 0.0, 1.0, 4.0, 8.0),
                Block.box(1.0, 1.0, 8.0, 16.0, 3.0, 9.0),
                Block.box(0.0, 1.0, 8.0, 1.0, 8.0, 16.0),
                Block.box(0.0, 1.0, 9.0, 16.0, 8.0, 16.0),
                Block.box(0.0, 8.0, 7.0, 16.0, 9.0, 16.0),
                Block.box(1.0, 1.0, 1.0, 16.0, 2.0, 8.0)
        );

        VoxelShape[] shapes = ShapeUtil.getESWShapes(N_PEDAL_L_SHAPE);

        E_PEDAL_L_SHAPE = shapes[0];
        S_PEDAL_L_SHAPE = shapes[1];
        W_PEDAL_L_SHAPE = shapes[2];

        shapes = ShapeUtil.getESWShapes(N_PEDAL_SHAPE);

        E_PEDAL_SHAPE = shapes[0];
        S_PEDAL_SHAPE = shapes[1];
        W_PEDAL_SHAPE = shapes[2];

        shapes = ShapeUtil.getESWShapes(N_PEDAL_R_SHAPE);

        E_PEDAL_R_SHAPE = shapes[0];
        S_PEDAL_R_SHAPE = shapes[1];
        W_PEDAL_R_SHAPE = shapes[2];

        shapes = ShapeUtil.getESWShapes(N_KEYBOARD_L_SHAPE);

        E_KEYBOARD_L_SHAPE = shapes[0];
        S_KEYBOARD_L_SHAPE = shapes[1];
        W_KEYBOARD_L_SHAPE = shapes[2];

        shapes = ShapeUtil.getESWShapes(N_KEYBOARD_M_SHAPE);

        E_KEYBOARD_M_SHAPE = shapes[0];
        S_KEYBOARD_M_SHAPE = shapes[1];
        W_KEYBOARD_M_SHAPE = shapes[2];

        shapes = ShapeUtil.getESWShapes(N_KEYBOARD_R_SHAPE);

        E_KEYBOARD_R_SHAPE = shapes[0];
        S_KEYBOARD_R_SHAPE = shapes[1];
        W_KEYBOARD_R_SHAPE = shapes[2];
    }

    protected PianoBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TONE, 0).setValue(PART, PianoPart.PEDAL).setValue(DELAY, false));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        PianoPart part = pState.getValue(PART);
        return switch (pState.getValue(FACING)) {
            case NORTH -> switch (part) {
                case PEDAL -> N_PEDAL_SHAPE;
                case PEDAL_R -> N_PEDAL_R_SHAPE;
                case KEYBOARD_R -> N_KEYBOARD_R_SHAPE;
                case KEYBOARD_M -> N_KEYBOARD_M_SHAPE;
                case KEYBOARD_L -> N_KEYBOARD_L_SHAPE;
                case PEDAL_L -> N_PEDAL_L_SHAPE;
            };
            case SOUTH -> switch (part) {
                case PEDAL -> S_PEDAL_SHAPE;
                case PEDAL_R -> S_PEDAL_R_SHAPE;
                case KEYBOARD_R -> S_KEYBOARD_R_SHAPE;
                case KEYBOARD_M -> S_KEYBOARD_M_SHAPE;
                case KEYBOARD_L -> S_KEYBOARD_L_SHAPE;
                case PEDAL_L -> S_PEDAL_L_SHAPE;
            };
            case WEST -> switch (part) {
                case PEDAL -> W_PEDAL_SHAPE;
                case PEDAL_R -> W_PEDAL_R_SHAPE;
                case KEYBOARD_R -> W_KEYBOARD_R_SHAPE;
                case KEYBOARD_M -> W_KEYBOARD_M_SHAPE;
                case KEYBOARD_L -> W_KEYBOARD_L_SHAPE;
                case PEDAL_L -> W_PEDAL_L_SHAPE;
            };
            case EAST -> switch (part) {
                case PEDAL -> E_PEDAL_SHAPE;
                case PEDAL_R -> E_PEDAL_R_SHAPE;
                case KEYBOARD_R -> E_KEYBOARD_R_SHAPE;
                case KEYBOARD_M -> E_KEYBOARD_M_SHAPE;
                case KEYBOARD_L -> E_KEYBOARD_L_SHAPE;
                case PEDAL_L -> E_PEDAL_L_SHAPE;
            };
            default -> Shapes.empty();
        };
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
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
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        PianoPart part = pState.getValue(PART);
        if (!pLevel.isClientSide && part != LAST_PART) {
            BlockState state = pState;
            while ((state = this.getCombinedState(state.getValue(PART), state)) != null) {
                BlockPos pos = pPos.relative(this.getCombinedDirection(pState.getValue(PART), pState.getValue(FACING)));
                pLevel.setBlock(pos, state, ExampleBlock.UPDATE_ALL);
                pLevel.blockUpdated(pPos, Blocks.AIR);
                state.updateNeighbourShapes(pLevel, pPos, ExampleBlock.UPDATE_ALL);
                pPos = pos;
                pState = state;
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        Direction facing = pContext.getHorizontalDirection().getOpposite();
        BlockPos thisPos = pContext.getClickedPos();
        Level level = pContext.getLevel();

        List<BlockPos> posList = new ArrayList<>();
        posList.add(thisPos.relative(facing.getCounterClockWise()));
        posList.add(thisPos.relative(facing.getClockWise()));
        posList.add(thisPos.relative(Direction.UP));
        posList.add(thisPos.relative(facing.getCounterClockWise()).relative(Direction.UP));
        posList.add(thisPos.relative(facing.getClockWise()).relative(Direction.UP));

        boolean flag = true;
        int size = posList.size();
        for (int i = 0; i < size; i++) {
            BlockPos pos = posList.get(i);
            if (!level.getBlockState(pos).canBeReplaced() || !level.getWorldBorder().isWithinBounds(pos)) {
                flag = false;
                break;
            }
        }
        return flag ? this.defaultBlockState().setValue(FACING, facing) : null;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (pDirection == this.getCombinedDirection(pState.getValue(PART), pState.getValue(FACING))) {
            return pNeighborState.is(this) && pNeighborState.getValue(PART) != pState.getValue(PART) ? pState.setValue(TONE, pNeighborState.getValue(TONE)).setValue(DELAY, pNeighborState.getValue(DELAY)) : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    @Override
    public @Nullable PianoBlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new PianoBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(@NotNull BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        PianoPart part = pState.getValue(PART);
        boolean flag = part == PianoPart.KEYBOARD_L || part == PianoPart.KEYBOARD_M || part == PianoPart.KEYBOARD_R;
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof PianoBlockEntity) {
                if (flag) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, (MenuProvider) blockEntity, pPos);
                    return InteractionResult.CONSUME;
                } else if (part == PianoPart.PEDAL) {
                    boolean delay = pState.getValue(DELAY);
                    this.setDelay(pState, pLevel, pPos, !delay);
                    return InteractionResult.CONSUME;
                } else {
                    return InteractionResult.PASS;
                }
            }
        } else {
            return flag || part == PianoPart.PEDAL ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    public void setDelay(BlockState state, Level level, BlockPos pos, boolean delay) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(DELAY, delay), Block.UPDATE_ALL);

            BlockPos blockPos = pos;
            List<Integer> tones = Lists.newArrayList();
            while (level.getBlockEntity(blockPos) instanceof PianoBlockEntity pianoBlockEntity && (pianoBlockEntity.isSoundDelay() != delay)) {
                BlockState state1 = level.getBlockState(blockPos);
                if (!delay && (state1.getValue(PART) == PianoPart.KEYBOARD_L || state1.getValue(PART) == PianoPart.KEYBOARD_M || state1.getValue(PART) == PianoPart.KEYBOARD_R)) {
                    tones.addAll(
                            Util.toIntegerList(((MinecraftServerExtender) ServerLifecycleHooks.getCurrentServer()).betterSound$getModDataManager().getLastTones(blockPos))
                    );
                    ModNetwork.broadcast(new CPianoBlockStopPacket(blockPos, Util.toIntArray(tones)));
                }
                pianoBlockEntity.setSoundDelay(delay);
                pianoBlockEntity.setChanged();
                Direction combinedDir = this.getCombinedDirection(state1.getValue(PART), state1.getValue(FACING));
                blockPos = blockPos.relative(combinedDir);
            }
        }
    }

    public void playSound(ServerPlayer pPlayer, BlockState pState, Level pLevel, BlockPos pPos) {
        if (!pLevel.isClientSide) {
            if (pPlayer != null) {
                ModNetwork.broadcastBut(new CPianoBlockPlayNotePacket(pPlayer.getUUID(), pPos, pState.getValue(PianoBlock.TONE), false, false), pPlayer);
            } else
                ModNetwork.broadcast(new CPianoBlockPlayNotePacket(null, pPos, pState.getValue(PianoBlock.TONE), false, false));
            pLevel.blockEvent(pPos, this, 0, 0);
        }
    }

    public void stopSound(ServerPlayer pPlayer, BlockState pState, Level pLevel, BlockPos pPos) {
        if (!pLevel.isClientSide) {
            if (pPlayer != null) {
                ModNetwork.broadcastBut(new CPianoBlockPlayNotePacket(pPlayer.getUUID(), pPos, pState.getValue(PianoBlock.TONE), true, false), pPlayer);
            } else
                ModNetwork.broadcast(new CPianoBlockPlayNotePacket(null, pPos, pState.getValue(PianoBlock.TONE), true, false));
        }
    }

    private void spawnParticles(Level pLevel, BlockPos pPos) {
        if (pLevel.isClientSide) {
            int count = 8;
            int gap = count / 4;
            for (int i = 0; i < count; i++) {
                int direction;

                if (i < gap) direction = 0;
                else if (i < 2 * gap) direction = 1;
                else if (i < 3 * gap) direction = 2;
                else direction = 3;

                double rSpeed = 0.1D + (Math.random() * 2.0D - 1.0D) * 0.04D;
                double dr = 0.66D;
                double theta = 0.5D * Math.PI * direction + (Math.random() * 2.0D - 1.0D) * 0.25D * Math.PI;
                double cosTheta = Math.cos(theta);
                double sinTheta = Math.sin(theta);
                double zSpeed = rSpeed * cosTheta;
                double xSpeed = rSpeed * sinTheta;
                double ySpeed = (Math.random() * 2.0D - 1.0D) * 0.03D;
                double dz = dr * cosTheta;
                double dx = dr * sinTheta;
                pLevel.addParticle(ModParticleTypes.BLACK_NOTE.get(), (double) pPos.getX() + 0.5D + dx, (double) pPos.getY() + 0.5D, (double) pPos.getZ() + 0.5D + dz, xSpeed, ySpeed, zSpeed);
            }
        }
    }

    @Override
    public boolean triggerEvent(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, int pId, int pParam) {
        this.spawnParticles(pLevel, pPos);
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TONE, PART, FACING, DELAY);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return TickableBlockEntity.createTicker();
    }

    @Override
    public Direction getCombinedDirection(PianoPart part, Direction facing) {
        return switch (part) {
            case PEDAL, PEDAL_L -> facing.getCounterClockWise();
            case PEDAL_R -> Direction.UP;
            case KEYBOARD_R, KEYBOARD_M -> facing.getClockWise();
            case KEYBOARD_L -> Direction.DOWN;
        };
    }

    @Override
    public BlockState getCombinedState(PianoPart part, BlockState state) {
        return switch (part) {
            case PEDAL -> state.setValue(PART, PianoPart.PEDAL_R);
            case PEDAL_R -> state.setValue(PART, PianoPart.KEYBOARD_R);
            case KEYBOARD_R -> state.setValue(PART, PianoPart.KEYBOARD_M);
            case KEYBOARD_M -> state.setValue(PART, PianoPart.KEYBOARD_L);
            case KEYBOARD_L -> state.setValue(PART, PianoPart.PEDAL_L);
            case PEDAL_L -> null;
        };
    }
}
