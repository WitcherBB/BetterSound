package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.PianoStoolBlock;
import com.witcherbb.bettersound.blocks.entity.utils.Sittable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PianoStoolBlockEntity extends BlockEntity implements Sittable<PianoStoolBlockEntity> {
    List<Player> passengers = Lists.newArrayList();

    public PianoStoolBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.PIANO_STOOL_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
    }

    @Override
    public void positionPassenger(Player player) {
        if (this.hasPassenger()) {
            BlockPos pos = this.getBlockPos();
            double d0 = pos.getY() + this.getPassengersRidingOffset();
            player.setPos(pos.getX() + 0.5, d0, pos.getZ() + 0.5);
        }
    }

    @Override
    @NotNull
    public Vec3 getDismountLocationForPassenger(Player passenger) {
        Direction facing = this.getBlockState().getValue(PianoStoolBlock.FACING);
        Direction dismountDir = facing.getClockWise();
        Vec3i vec3i = dismountDir.getNormal();
        BlockPos pos = this.getBlockPos();
        return new Vec3(pos.getX() + 0.5 + vec3i.getX(), pos.getY() + vec3i.getY(), pos.getZ() + 0.5 + vec3i.getZ());
    }

    @Override
    public PianoStoolBlockEntity get() {
        return this;
    }

    @Override
    public float yRot() {
        return this.getBlockState().getValue(PianoStoolBlock.FACING).toYRot();
    }

    @Override
    public void addPassenger(Player passenger) {
        passengers.add(passenger);
    }

    @Override
    public boolean canAddPassenger(Player passenger) {
        return passengers.isEmpty();
    }

    @Override
    public boolean hasPassenger() {
        return !passengers.isEmpty();
    }

    @Override
    public void removePassenger(Player passenger) {
        passengers.remove(0);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.1;
    }


}
