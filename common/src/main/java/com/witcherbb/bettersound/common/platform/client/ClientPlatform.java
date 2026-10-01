package com.witcherbb.bettersound.common.platform.client;

import java.util.Objects;

public final class ClientPlatform {
    private static ClientHooks hooks;

    public static void install(ClientHooks clientHooks) {
        hooks = Objects.requireNonNull(clientHooks, "clientHooks");
    }

    public static ClientHooks hooks() {
        if (hooks == null) {
            throw new IllegalStateException("ClientPlatform 未初始化：loader 侧要先调用 ClientPlatform.install(...)");
        }
        return hooks;
    }

    private ClientPlatform() {
    }
}
