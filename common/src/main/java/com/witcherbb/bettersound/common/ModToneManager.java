package com.witcherbb.bettersound.common;

import com.witcherbb.bettersound.common.utils.LastToneMap;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 记录每台钢琴上每位玩家最后弹奏的音符（延音/停止逻辑用）。
 * <p><b>仅服务端运行期使用（含单人模式内置服务端）</b>，客户端禁止调用。</p>
 */
public class ModToneManager {
    private static volatile ModToneManager INSTANCE = null;
    private final LastToneMap lastToneMap = LastToneMap.create();

    public static void destroy() {
        INSTANCE = null;
    }

    @NotNull
    public static ModToneManager getInstance() {
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
        return lastToneMap;
    }

    public void putLastTone(BlockPos pos, UUID uuid, Integer tone) {
        this.lastToneMap.put(pos, uuid, tone);
    }

    public boolean removeLastTone(BlockPos pos, UUID uuid, Integer tone) {
        return this.lastToneMap.remove(pos, uuid, tone);
    }

    public int[] getLastTones(BlockPos pos) {
        return this.lastToneMap.getByPos(pos);
    }
}
