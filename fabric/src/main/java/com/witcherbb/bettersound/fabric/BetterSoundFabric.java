package com.witcherbb.bettersound.fabric;

import com.witcherbb.bettersound.BetterSoundCommon;
import com.witcherbb.bettersound.fabric.config.FabricCommonConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric 入口。
 *
 * <p>没有 {@code @ExpectPlatform} 的情况下，"platform 相关实现"不会自动接线，
 * 所以这里必须自己把 loader 侧的实现 install / 注册一遍（forge 侧见 {@code BetterSoundForge}）：
 * <ol>
 *   <li>{@link BetterSoundCommon#init()} —— 网络通道/数据包 + 粒子类型等 common 侧共用初始化。</li>
 * </ol>
 */
public class BetterSoundFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        registerEvents();
        BetterSoundCommon.init();
    }

    private static void registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register(EventHandlers::onServerStopped);
    }

    private static final class EventHandlers {
        static void onServerStopped(MinecraftServer server) {
        }
    }
}
