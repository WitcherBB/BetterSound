package com.witcherbb.bettersound.platform.client.fabric;

import com.witcherbb.bettersound.fabric.config.FabricClientConfig;

public final class ClientConfigImpl {
    public static boolean showTone() {
        return FabricClientConfig.HANDLER.instance().isShowTone();
    }
}
