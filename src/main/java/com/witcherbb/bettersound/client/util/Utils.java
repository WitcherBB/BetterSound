package com.witcherbb.bettersound.client.util;

import com.witcherbb.bettersound.blocks.PianoBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Utils {

    public static <T extends Block> boolean aimedBlock(Player player, BlockPos aimedPos, Class<T> clazz) {
        Level level = player.level();

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos hitPos = blockHitResult.getBlockPos();
            return aimedPos.equals(hitPos) && clazz.isInstance(level.getBlockState(hitPos).getBlock());
        }
        return false;
    }
}
