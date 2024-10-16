package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.blocks.extensions.SpectatorInvalidBlock;
import com.witcherbb.bettersound.items.TunerItem;
import com.witcherbb.bettersound.mixins.extenders.MinecraftServerExtender;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockStopPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToneBlock extends AbstractPianoBlock implements SpectatorInvalidBlock {
    public static IntegerProperty TONE = IntegerProperty.create("tones", 0, 87);
    public static BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected ToneBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ToneBlockEntity(pPos, pState);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if (!pLevel.isClientSide) {
            boolean flag = pLevel.hasNeighborSignal(pPos);
            if (flag != pState.getValue(POWERED)) {
                if (flag) {
                    this.playSound(null, pState.getValue(TONE), (byte) 100, pLevel, pPos);
                }
                pLevel.setBlock(pPos, pState.setValue(POWERED, flag), Block.UPDATE_ALL);
            }
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof ToneBlockEntity blockEntity) {
                boolean flag1 = hasNeighborPedalSteped(pLevel, pPos);
                boolean flag2 = blockEntity.isSoundDelay();
                if (flag1 != flag2) {
                    if (!flag1) {
                        blockEntity.setSoundDelay(false);
                        int[] tones = ((MinecraftServerExtender) ServerLifecycleHooks.getCurrentServer()).betterSound$getModDataManager().getLastTones(pPos);
                        ModNetwork.broadcast(new CPianoBlockStopPacket(pPos, tones));
                    } else {
                        blockEntity.setSoundDelay(true);
                    }
                    blockEntity.setChanged();
                }
            }
        }
    }

    @Override
    public InteractionResult use(@NotNull BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof ToneBlockEntity && pPlayer.getItemInHand(pHand).getItem() instanceof TunerItem) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, (MenuProvider) blockEntity, pPos);
                return InteractionResult.CONSUME;
            }
            pLevel.setBlock(pPos, pState.cycle(TONE), Block.UPDATE_ALL);
            this.playSound(null, pLevel.getBlockState(pPos).getValue(TONE), (byte) 100, pLevel, pPos);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }


    public void playSound(@Nullable ServerPlayer pPlayer, int tone, byte volume, Level pLevel, BlockPos pPos) {
        if (pPlayer != null) {
            ModNetwork.broadcastBut(new CPianoBlockPlayNotePacket(pPlayer.getUUID(), Vec3.atCenterOf(pPos), tone, volume, false, false), pPlayer);
        } else
            ModNetwork.broadcast(new CPianoBlockPlayNotePacket(null, Vec3.atCenterOf(pPos), tone, volume, false, false));
        pLevel.blockEvent(pPos, this, 0, 0);
    }

    public void stopSound(@Nullable ServerPlayer pPlayer, int tone, Level pLevel, BlockPos pPos) {
        if (pPlayer != null) {
            ModNetwork.broadcastBut(new CPianoBlockPlayNotePacket(pPlayer.getUUID(), Vec3.atCenterOf(pPos), tone, (byte) 0, true, false), pPlayer);
        }
        else ModNetwork.broadcast(new CPianoBlockPlayNotePacket(null, Vec3.atCenterOf(pPos), tone, (byte) 0, true, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TONE, POWERED);
    }
}
