package com.witcherbb.bettersound.common;

import com.witcherbb.bettersound.common.utils.LastToneMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

/**
 * 记录每台钢琴上每位玩家最后弹奏的音符（延音/停止逻辑用）。
 * <p><b>仅服务端运行期使用（含单人模式内置服务端）</b>，客户端禁止调用；
 * 入口有 fail-fast 守卫，误调立即抛异常并附调用栈，便于定位误调点。</p>
 */
public class ModToneManager {
    private static volatile ModToneManager INSTANCE = null;
    private final LastToneMap lastToneMap = LastToneMap.create();

    /** 守卫：必须在“服务端运行期”调用（单人内置服务端同样满足，IntegratedServer 也是 MinecraftServer） */
    private static void ensureServerSide() {
        if (com.witcherbb.bettersound.common.platform.Platform.hooks().currentServer() == null) {
            throw new IllegalStateException(
                    Component.translatable("exception.bettersound.modtonemanager.onlyserver").getString()
                            + "\n" + Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    }

    public static void destroy() {
        ensureServerSide();
        INSTANCE = null;
    }

    @NotNull
    public static ModToneManager getInstance() {
        ensureServerSide();
        ModToneManager inst = INSTANCE;
        if (inst == null) {
            inst = new ModToneManager();
            INSTANCE = inst;
        }
        return inst;
    }

    private ModToneManager() {
    }

    @NotNull
    public LastToneMap getLastToneMap() {
        ensureServerSide();
        return lastToneMap;
    }

    public void putLastTone(BlockPos pos, UUID uuid, Integer tone) {
        ensureServerSide();
        this.lastToneMap.put(pos, uuid, tone);
    }

    public boolean removeLastTone(BlockPos pos, UUID uuid, Integer tone) {
        ensureServerSide();
        return this.lastToneMap.remove(pos, uuid, tone);
    }

    public int[] getLastTones(BlockPos pos) {
        ensureServerSide();
        return this.lastToneMap.getByPos(pos);
    }
}
