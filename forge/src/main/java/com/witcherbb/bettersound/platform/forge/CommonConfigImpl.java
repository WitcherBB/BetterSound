package com.witcherbb.bettersound.platform.forge;

import com.witcherbb.bettersound.forge.ForgeCommonConfig;

public final class CommonConfigImpl {
    public static int midiMaxSustainBars() {
        return ForgeCommonConfig.COMMON.midiMaxSustainBars.get();
    }

    public static double releaseSubsections() {
        return ForgeCommonConfig.COMMON.releaseSubsections.get();
    }
}
