package com.witcherbb.bettersound.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class Utils {

    public static <T extends Block> boolean aimedBlock(Player player, BlockPos aimedPos, Class<T> clazz) {
        Level level = player.level();

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos hitPos = blockHitResult.getBlockPos();
            return clazz.isInstance(level.getBlockState(hitPos).getBlock()) && aimedPos.equals(hitPos);
        }
        return false;
    }
}
