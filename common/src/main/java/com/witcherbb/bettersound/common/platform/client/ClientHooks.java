package com.witcherbb.bettersound.common.platform.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public interface ClientHooks {
    /**
     * 创建一个「只在 GUI 里生效」的按键绑定。
     *
     * <p>Forge 用 {@code KeyConflictContext.GUI} 限定冲突范围；vanilla / 其它 loader 没有这个概念，
     * 直接 new 一个普通 {@link KeyMapping} 即可。
     */
    KeyMapping createGuiKeyMapping(String name, InputConstants.Type type, int keyCode, String category);

    /** 把按键绑定序列化成 options 文件里的一行（Forge 会额外带上按键修饰键）。 */
    String serializeKeyMapping(KeyMapping mapping);

    /** 把 options 文件里读到的字符串还原到按键绑定上（与 {@link #serializeKeyMapping} 配对）。 */
    void applySerializedKeyMapping(KeyMapping mapping, String serialized);

    /**
     * 按键绑定是否匹配
     * @param mapping {@link KeyMapping}
     * @param key {@link InputConstants.Key}
     * @return 匹配则返回{@code true}, 否则返回{@code false}
     */
    boolean isActiveAndMatchesKey(KeyMapping mapping, InputConstants.Key key);

    default boolean hasKeyModifierConflict(KeyMapping mapping1, KeyMapping mapping2) {
        return false;
    }
}
