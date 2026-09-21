package com.witcherbb.bettersound.config;

import com.witcherbb.bettersound.ClientConfig;
import com.witcherbb.bettersound.CommonConfig;
import com.witcherbb.bettersound.common.config.Configs;

/** Forge 侧配置读取实现：直接转发到 {@code ForgeConfigSpec} 定义的值上。 */
public final class ForgeConfigValues implements Configs.Values {

    @Override
    public int midiMaxSustainBars() {
        return CommonConfig.COMMON.midiMaxSustainBars.get();
    }

    @Override
    public boolean showTone() {
        // 只在客户端渲染器里被调用；专用服务端不会触发
        return ClientConfig.CLIENT.showTone.get();
    }
}
