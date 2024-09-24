package com.witcherbb.bettersound.client.util;

import com.witcherbb.bettersound.blocks.PianoBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Utils {
    public static boolean aimedBlock(Player player, BlockPos blockPos) {
        Level level = player.level();

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos hitPos = blockHitResult.getBlockPos();
            return blockPos.equals(hitPos) && level.getBlockState(hitPos).getBlock() instanceof PianoBlock;
        }
        return false;
    }
}
