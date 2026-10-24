package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.blocks.extensions.SpectatorInvalidBlock;
import com.witcherbb.bettersound.common.ModToneManager;
import com.witcherbb.bettersound.items.TunerItem;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.client.COpenPianoScreenPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoPlaySingleNotePacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoStopNotesExceptPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoStopSingleNotePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof ToneBlockEntity toneBlockEntity) {
                boolean flag = pLevel.hasNeighborSignal(pPos);
                if (flag != pState.getValue(POWERED)) {
                    if (flag) {
                        this.playSound(null, pState.getValue(TONE), (byte) 100, pLevel, pPos,
                                !toneBlockEntity.isSoundDelay());
                    }
                    pLevel.setBlock(pPos, pState.setValue(POWERED, flag), Block.UPDATE_ALL);
                }
                boolean flag1 = hasNeighborPedalSteped(pLevel, pPos);
                boolean flag2 = toneBlockEntity.isSoundDelay();
                if (flag1 != flag2) {
                    if (!flag1) {
                        toneBlockEntity.setSoundDelay(false);
                        int[] tones = ModToneManager.getInstance().getLastTones(pPos);
                        ModNetwork.broadcast(new CPianoStopNotesExceptPacket(pPos, tones));
                    } else {
                        toneBlockEntity.setSoundDelay(true);
                    }
                    toneBlockEntity.setChanged();
                }
            }
        }
    }

    @Override
    public InteractionResult use(@NotNull BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof ToneBlockEntity toneBlockEntity) {
                if (pPlayer.getItemInHand(pHand).getItem() instanceof TunerItem) {
                    ModNetwork.sendToPlayer(new COpenPianoScreenPacket(pPos), (ServerPlayer) pPlayer);
                    return InteractionResult.CONSUME;
                }
                pLevel.setBlock(pPos, pState.cycle(TONE), Block.UPDATE_ALL);
                this.playSound(null, pLevel.getBlockState(pPos).getValue(TONE), (byte) 100, pLevel, pPos,
                        !toneBlockEntity.isSoundDelay());
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }


    public void playSound(@Nullable ServerPlayer pPlayer, int tone, byte volume, Level pLevel, BlockPos pPos, boolean isShort) {
        if (pPlayer != null) {
            ModNetwork.broadcastBut(new CPianoPlaySingleNotePacket(pPlayer.getUUID(), pPos, Vec3.atCenterOf(pPos), tone, volume, false, isShort), pPlayer);
        } else
            ModNetwork.broadcast(new CPianoPlaySingleNotePacket(null, pPos, Vec3.atCenterOf(pPos), tone, volume, false, isShort));
        pLevel.blockEvent(pPos, this, 0, 0);
    }

    public void stopSound(@Nullable ServerPlayer pPlayer, int tone, Level pLevel, BlockPos pPos) {
        if (pPlayer != null) {
            ModNetwork.broadcastBut(new CPianoStopSingleNotePacket(pPlayer.getUUID(), pPos, tone), pPlayer);
        } else {
            ModNetwork.broadcast(new CPianoStopSingleNotePacket(null, pPos, tone));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TONE, POWERED);
    }
}
