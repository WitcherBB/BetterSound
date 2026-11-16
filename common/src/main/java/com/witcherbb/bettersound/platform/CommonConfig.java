package com.witcherbb.bettersound.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class CommonConfig {
    @ExpectPlatform
    public static int midiMaxSustainBars() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static double releaseSubsections() {
        throw new AssertionError();
    }
}
