package com.witcherbb.bettersound.common.utils;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LastToneMap extends HashMap<BlockPos, HashMap<UUID, List<Integer>>> {
    private LastToneMap() {
    }

    public void put(BlockPos pos, UUID uuid, Integer tone) {
        HashMap<UUID, List<Integer>> lastToneMap = this.computeIfAbsent(pos, k -> new HashMap<>());
        List<Integer> toneList = lastToneMap.computeIfAbsent(uuid, k -> new ArrayList<>());
        toneList.add(tone);
    }

    public boolean remove(BlockPos pos, UUID uuid, Integer tone) {
        HashMap<UUID, List<Integer>> map = this.get(pos);
        if (map == null) return false;
        List<Integer> toneList = map.get(uuid);
        if (toneList == null) return false;
        boolean flag = toneList.remove(tone);
        if (toneList.isEmpty()) {
            map.remove(uuid);
            if (map.isEmpty()) {
                this.remove(pos);
            }
        }
        return flag;
    }

    public int[] getByPos(BlockPos pos) {
        HashMap<UUID, List<Integer>> map = this.get(pos);
        if (map == null) return new int[0];
        List<Integer> newTones = new ArrayList<>();
        for (List<Integer> tones : map.values()) {
            newTones.addAll(tones);
        }
        return Util.toIntArray(newTones);
    }

    public static LastToneMap create() {
        return new LastToneMap();
    }
}
