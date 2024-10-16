package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.blocks.extensions.SpectatorInvalidBlock;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPianoBlock extends BaseEntityBlock implements SpectatorInvalidBlock {
    public static IntegerProperty TONE = IntegerProperty.create("tones", 0, 87);

    protected AbstractPianoBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    public static boolean hasNeighborPedalSteped(Level level, BlockPos pos) {
        BlockState state;
        if ((state = level.getBlockState(pos.north())).getBlock() instanceof SustainPedalBlock
                && state.getValue(SustainPedalBlock.FACING) == Direction.NORTH && state.getValue(SustainPedalBlock.STEPED))
            return true;
        else if ((state = level.getBlockState(pos.east())).getBlock() instanceof SustainPedalBlock
                && state.getValue(SustainPedalBlock.FACING) == Direction.EAST && state.getValue(SustainPedalBlock.STEPED))
            return true;
        else if ((state = level.getBlockState(pos.south())).getBlock() instanceof SustainPedalBlock
                && state.getValue(SustainPedalBlock.FACING) == Direction.SOUTH && state.getValue(SustainPedalBlock.STEPED))
            return true;
        else return (state = level.getBlockState(pos.west())).getBlock() instanceof SustainPedalBlock
                    && state.getValue(SustainPedalBlock.FACING) == Direction.WEST && state.getValue(SustainPedalBlock.STEPED);
    }

    /** Server side */
    public abstract void playSound(@Nullable ServerPlayer pPlayer, int tone, byte volume, Level pLevel, BlockPos pPos);

    /** Server side */
    public abstract void stopSound(@Nullable ServerPlayer pPlayer, int tone, Level pLevel, BlockPos pPos);

    protected void spawnParticles(Level pLevel, BlockPos pPos) {
        if (pLevel.isClientSide) {
            int count = 8;
            int directionCount = 4;
            int gap = count / directionCount;
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
        super.triggerEvent(pState, pLevel, pPos, pId, pParam);
        this.spawnParticles(pLevel, pPos);
        return true;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return TickableBlockEntity.createTicker();
    }
}
