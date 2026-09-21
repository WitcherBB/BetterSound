package com.witcherbb.bettersound.blocks.entity.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public interface Sittable<BE extends BlockEntity> {
    void positionPassenger(Player player);

    Vec3 getDismountLocationForPassenger(Player passenger);

    BE get();

    float yRot();

    void addPassenger(Player passenger);

    boolean canAddPassenger(Player passenger);

    boolean hasPassenger();

    void removePassenger(Player passenger);

    double getPassengersRidingOffset();

    boolean isRemoved();

    default BlockPos getBlockPos() {
        return get().getBlockPos();
    }
}
