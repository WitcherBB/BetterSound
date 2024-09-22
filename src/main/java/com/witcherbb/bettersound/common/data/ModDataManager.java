package com.witcherbb.bettersound.common.data;

import com.witcherbb.bettersound.common.utils.LastToneMap;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public class ModDataManager {
    private final LastToneMap lastToneMap = LastToneMap.create();

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
