package com.witcherbb.bettersound.entities;

import com.witcherbb.bettersound.blocks.PianoStoolBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FakeStoolEntity extends Entity {
    private BlockState stoolBlockState;
    private BlockPos stoolPos;

    public FakeStoolEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void removePassenger(@NotNull Entity entity) {
        super.removePassenger(entity);
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity livingEntity) {
        if (this.stoolBlockState == null || this.stoolPos == null) {
            return super.getDismountLocationForPassenger(livingEntity);
        }
        Direction direction = this.stoolBlockState.getValue(PianoStoolBlock.FACING);
        BlockPos dismountPos = null;
        for (int i = 0; i < 4; i++) {
            BlockPos destination = this.stoolPos.relative(direction);
            if (this.level().getBlockState(destination).isAir()) {
                dismountPos = destination;
                break;
            }
            direction = direction.getClockWise();
        }

        if (dismountPos == null) {
            if (this.level().getBlockState(this.stoolPos.relative(Direction.Axis.Y, 2)).isAir()) {
                dismountPos = this.stoolPos.relative(Direction.Axis.Y, 1);
            } else {
                dismountPos = this.stoolPos;
            }
        }

        return new Vec3(dismountPos.getX() + 0.5, dismountPos.getY(), dismountPos.getZ() + 0.5);
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.5;
    }

    public void setStool(BlockState blockState) {
        this.stoolBlockState = blockState;
    }

    public void setStoolPos(BlockPos stoolPos) {
        this.stoolPos = stoolPos;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }
}
