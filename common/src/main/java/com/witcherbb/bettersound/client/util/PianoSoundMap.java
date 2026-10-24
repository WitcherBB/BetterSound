package com.witcherbb.bettersound.client.util;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.client.resources.sounds.PianoSoundInstance;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PianoSoundMap {
    private static final PianoSoundInstance[] defaultPianoSoundInstances = createEmptyPianoSoundInstances();
    private static final Map<UUID, PianoSoundInstance[]> defaultUuidMap = ImmutableMap.of();

    private final Map<Long, Map<UUID, PianoSoundInstance[]>> soundMap = new Long2ObjectOpenHashMap<>();

    public PianoSoundMap() {
    }

    public void put(BlockPos pos, UUID playerUUID, int tone, @NotNull PianoSoundInstance instance) {
        Map<UUID, PianoSoundInstance[]> uuidMap =
                this.soundMap.computeIfAbsent(pos.asLong(), posK -> new Object2ObjectOpenHashMap<>());
        PianoSoundInstance[] instanceMap =
                uuidMap.computeIfAbsent(playerUUID, uuidK -> createEmptyPianoSoundInstances());
        instanceMap[tone] = instance;
    }

    public PianoSoundInstance get(BlockPos pos, UUID playerUUID, int tone) {
        Map<UUID, PianoSoundInstance[]> uuidMap = this.soundMap.getOrDefault(pos.asLong(), defaultUuidMap);
        PianoSoundInstance[] instances = uuidMap.getOrDefault(playerUUID, defaultPianoSoundInstances);
        return instances[tone];
    }

    public @Nullable PianoSoundInstance remove(BlockPos pos, UUID playerUUID, int tone) {
        Map<UUID, PianoSoundInstance[]> uuidMap = this.soundMap.getOrDefault(pos.asLong(), defaultUuidMap);
        PianoSoundInstance[] instances = uuidMap.getOrDefault(playerUUID, defaultPianoSoundInstances);
        if (instances[tone] == null) return null;
        PianoSoundInstance old = instances[tone];
        instances[tone] = null;
        this.pruneEmpty(pos, playerUUID);
        return old;
    }

    public @NotNull List<PianoSoundInstance> remove(BlockPos pos) {
        Map<UUID, PianoSoundInstance[]> removedUuidMap =
                Objects.requireNonNullElse(this.soundMap.remove(pos.asLong()), defaultUuidMap);

        List<PianoSoundInstance> removedInstances = new ArrayList<>();
        for (PianoSoundInstance[] instances : removedUuidMap.values()) {
            // 交出真正在响的实例
            removedInstances.addAll(Arrays.stream(instances).filter(Objects::nonNull).toList());
//            for (PianoSoundInstance instance : instances) {
//                if (instance != null) {
//                    removedInstances.add(instance);
//                }
//            }
        }
        return removedInstances;
    }

    /**
     * 摘走这台钢琴上所有“已经不需要再响”的实例，只把 {@code tones} 点名的那些留下。
     *
     * <p>{@code tones} 是玩家此刻还按着的音（{@code ModToneManager.getLastTones}），
     * 它们要继续响，也必须继续留在簿记里——等玩家真正抬手时再通过
     * {@code tryToStopPianoSound} -> {@code get} 停掉。
     * 所以这里只能逐个音调摘，绝不可以把整个位置一起清空。</p>
     *
     * @return 移除的实例
     */
    public List<PianoSoundInstance> removeButLast(BlockPos pos, int[] tones) {
        if (ArrayUtils.isEmpty(tones)) return this.remove(pos);

        Map<UUID, PianoSoundInstance[]> uuidMap = this.soundMap.getOrDefault(pos.asLong(), defaultUuidMap);

        List<PianoSoundInstance> removedInstances = new ArrayList<>();
        Iterator<Map.Entry<UUID, PianoSoundInstance[]>> players = uuidMap.entrySet().iterator();
        while (players.hasNext()) {
            PianoSoundInstance[] instances = players.next().getValue();
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null || ArrayUtils.contains(tones, i)) continue;
                removedInstances.add(instances[i]);
                instances[i] = null;
            }
            if (isAllEmpty(instances)) players.remove();
        }
        if (!removedInstances.isEmpty() && uuidMap.isEmpty()) this.soundMap.remove(pos.asLong());
        return removedInstances;
    }

    public void clear() {
        this.soundMap.clear();
    }

    private void pruneEmpty(BlockPos pos, UUID playerUUID) {
        Map<UUID, PianoSoundInstance[]> uuidMap = this.soundMap.get(pos.asLong());
        PianoSoundInstance[] instances = uuidMap.get(playerUUID);
        if (instances == null) {
            return;
        }
        if (isAllEmpty(instances)) {
            uuidMap.remove(playerUUID);
            if (uuidMap.isEmpty()) {
                this.soundMap.remove(pos.asLong());
            }
        }
    }

    private static boolean isAllEmpty(PianoSoundInstance[] instances) {
        return Arrays.stream(instances).allMatch(Objects::isNull);
    }

    private static PianoSoundInstance[] createEmptyPianoSoundInstances() {
        return new PianoSoundInstance[Constants.PIANO_KEY_NUM];
    }

    public static PianoSoundMap create() {
        return new PianoSoundMap();
    }
}
