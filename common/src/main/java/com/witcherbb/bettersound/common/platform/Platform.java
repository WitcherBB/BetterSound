package com.witcherbb.bettersound.common.platform;

import java.util.Objects;

/** loader 差异服务的注入点：各 loader 在 mod 初始化时 {@link #install(LoaderHooks)}。 */
public final class Platform {

    private static LoaderHooks hooks;

    public static void install(LoaderHooks loaderHooks) {
        hooks = Objects.requireNonNull(loaderHooks, "loaderHooks");
    }

    public static LoaderHooks hooks() {
        if (hooks == null) {
            throw new IllegalStateException("Platform 未初始化：loader 侧要先调用 Platform.install(...)");
        }
        return hooks;
    }

    private Platform() {
    }
}
