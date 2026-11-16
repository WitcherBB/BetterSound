package com.witcherbb.bettersound.platform.client;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class ClientConfig {
    @ExpectPlatform
    public static boolean showTone() {
        throw new AssertionError();
    }
}
