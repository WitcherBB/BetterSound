package com.witcherbb.bettersound.platform.client.forge;

import com.witcherbb.bettersound.forge.ForgeClientConfig;

public final class ClientConfigImpl {
    public static boolean showTone() {
        return ForgeClientConfig.CLIENT.showTone.get();
    }
}
