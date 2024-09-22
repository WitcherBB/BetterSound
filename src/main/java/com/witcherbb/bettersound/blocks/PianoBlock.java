package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.items.TunerItem;
import com.witcherbb.bettersound.mixins.extenders.MinecraftServerExtender;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.CPianoBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.CPianoBlockStopPacket;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PianoBlock extends BaseEntityBlock {
    public static IntegerProperty TONE = IntegerProperty.create("tone", 0, 87);
    public static BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected PianoBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TONE, 0).setValue(POWERED, false));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new PianoBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(@NotNull BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof PianoBlockEntity && pPlayer.getItemInHand(pHand).getItem() instanceof TunerItem) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, (MenuProvider) blockEntity, pPos);
                return InteractionResult.CONSUME;
            }
            pLevel.setBlock(pPos, pState.cycle(TONE), Block.UPDATE_ALL);
            this.playSound(null, pLevel.getBlockState(pPos), pLevel, pPos);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if (!pLevel.isClientSide) {
            boolean flag = pLevel.hasNeighborSignal(pPos);
            if (flag != pState.getValue(POWERED)) {
                if (flag) {
                    this.playSound(null, pState, pLevel, pPos);
                }
                pLevel.setBlock(pPos, pState.setValue(POWERED, flag), Block.UPDATE_ALL);
            }
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof PianoBlockEntity blockEntity) {
                boolean flag1 = hasNeighborPedalSteped(pLevel, pPos);
                boolean flag2 = !blockEntity.isSoundDelay();
                if (flag1 != flag2) {
                    if (flag1) {
                        blockEntity.setSoundDelay(false);
                        int[] tones = ((MinecraftServerExtender) ServerLifecycleHooks.getCurrentServer()).getModDataManager().getLastTones(pPos);
                        ModNetwork.broadcast(new CPianoBlockStopPacket(pPos, tones));
                    } else {
                        blockEntity.setSoundDelay(true);
                    }
                    blockEntity.setChanged();
                }
            }
        }
    }



    public static boolean hasNeighborPedalSteped(Level level, BlockPos pos) {
        BlockState state;
        if ((state = level.getBlockState(pos.north())).getBlock() instanceof ConfinePedalBlock
                && state.getValue(ConfinePedalBlock.FACING) == Direction.NORTH && state.getValue(ConfinePedalBlock.STEPED))
            return true;
        else if ((state = level.getBlockState(pos.east())).getBlock() instanceof ConfinePedalBlock
                && state.getValue(ConfinePedalBlock.FACING) == Direction.EAST && state.getValue(ConfinePedalBlock.STEPED))
            return true;
        else if ((state = level.getBlockState(pos.south())).getBlock() instanceof ConfinePedalBlock
                && state.getValue(ConfinePedalBlock.FACING) == Direction.SOUTH && state.getValue(ConfinePedalBlock.STEPED))
            return true;
        else return (state = level.getBlockState(pos.west())).getBlock() instanceof ConfinePedalBlock
                    && state.getValue(ConfinePedalBlock.FACING) == Direction.WEST && state.getValue(ConfinePedalBlock.STEPED);
    }

    /**
     * 只在服务端调用
     * @param pPlayer
     * @param pState
     * @param pLevel
     * @param pPos
     */
    public void playSound(ServerPlayer pPlayer, BlockState pState, Level pLevel, BlockPos pPos) {
        if (pPlayer != null) {
            ModNetwork.broadcastBut(new CPianoBlockPlayNotePacket(pPlayer.getUUID(), pPos, pState.getValue(PianoBlock.TONE), false, false), pPlayer);
        } else
            ModNetwork.broadcast(new CPianoBlockPlayNotePacket(null, pPos, pState.getValue(PianoBlock.TONE), false, false));
        pLevel.blockEvent(pPos, this, 0, 0);
    }

    public void stopSound(ServerPlayer pPlayer, BlockState pState, Level pLevel, BlockPos pPos) {
        if (pPlayer != null) {
            ModNetwork.broadcastBut(new CPianoBlockPlayNotePacket(pPlayer.getUUID(), pPos, pState.getValue(PianoBlock.TONE), true, false), pPlayer);
        }
        else ModNetwork.broadcast(new CPianoBlockPlayNotePacket(null, pPos, pState.getValue(PianoBlock.TONE), true, false));
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
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        super.tick(pState, pLevel, pPos, pRandom);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TONE, POWERED);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return TickableBlockEntity.createTicker();
    }
}
