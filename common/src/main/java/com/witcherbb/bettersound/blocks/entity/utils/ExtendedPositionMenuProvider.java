package com.witcherbb.bettersound.blocks.entity.utils;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ExtendedPositionMenuProvider extends ExtendedMenuProvider {
    @Override
    default void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(((BlockEntity) this).getBlockPos());
    }
}
