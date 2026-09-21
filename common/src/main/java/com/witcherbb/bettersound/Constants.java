package com.witcherbb.bettersound;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * 与 loader 无关的公共常量，放在 common 模块里给 Forge / Fabric 两边共用。
 *
 * <p>注意：这里不能引用任何 loader 专属 API（net.minecraftforge.* / net.fabricmc.*），
 * 否则 common 模块无法单独编译——而各 loader 模块都依赖 common 的 jar。
 */
public final class Constants {

    /** 模组 id，必须与 META-INF/mods.toml、fabric.mod.json 中的一致。 */
    public static final String MOD_ID = "bettersound";

    /** 展示名。 */
    public static final String MOD_NAME = "Better Sound";

    /** 版本号（沿用原 {@code BetterSound.VERSION} 的硬编码值；与 gradle.properties 的 version 不同步）。 */
    public static final String VERSION = "1.20.1-0.0.0.0-demo";

    public static final Logger LOGGER = LogUtils.getLogger();

    private Constants() {
    }
}
