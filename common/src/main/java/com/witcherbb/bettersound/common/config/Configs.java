package com.witcherbb.bettersound.common.config;

import java.util.Objects;

/**
 * 与 loader 无关的配置读取入口。
 *
 * <p>配置的**定义**仍然在各 loader 侧（Forge 用 {@code ForgeConfigSpec}），
 * common 只通过这里的接口读值，这样内容类不必认识任何 loader 的配置 API。
 */
public final class Configs {

    /** 各 loader 提供实现（forge 侧见 {@code ForgeConfigValues}）。 */
    public interface Values {

        /** MIDI 延音踏板最长保持的小节数；0 表示不限制。 */
        int midiMaxSustainBars();

        /** 音阶方块顶部是否显示音阶（仅客户端会读）。 */
        boolean showTone();
    }

    private static Values values;

    public static void install(Values configValues) {
        values = Objects.requireNonNull(configValues, "configValues");
    }

    private static Values values() {
        if (values == null) {
            throw new IllegalStateException("Configs 未初始化：loader 侧要先调用 Configs.install(...)");
        }
        return values;
    }

    public static int midiMaxSustainBars() {
        return values().midiMaxSustainBars();
    }

    public static boolean showTone() {
        return values().showTone();
    }

    private Configs() {
    }
}
