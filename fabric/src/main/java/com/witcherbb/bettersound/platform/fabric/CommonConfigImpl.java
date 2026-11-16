package com.witcherbb.bettersound.platform.fabric;

import com.witcherbb.bettersound.fabric.config.FabricCommonConfig;

public final class CommonConfigImpl {
    public static int midiMaxSustainBars() {
        return FabricCommonConfig.HANDLER.instance().getMidiMaxSustainBars();
    }

    public static double releaseSubsections() {
        return FabricCommonConfig.HANDLER.instance().getReleaseSubsections();
    }
}
