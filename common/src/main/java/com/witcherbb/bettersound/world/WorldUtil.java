package com.witcherbb.bettersound.world;

import net.minecraft.world.level.Level;

public class WorldUtil {
    public static String getDimensionName(Level level) {
        return level.dimension().location().getPath();
    }
}
