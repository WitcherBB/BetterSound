package com.witcherbb.bettersound.client.util;

import com.witcherbb.bettersound.client.resources.sounds.PianoSoundInstance;
import com.witcherbb.bettersound.common.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class PianoSoundMap {
    private final HashMap<BlockPos, HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>>> soundMap = new HashMap<>();

    public void put(BlockPos pos, UUID playerUUID, int tone, PianoSoundInstance instance) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> uuidMap = this.soundMap.computeIfAbsent(pos, posK -> new HashMap<>());
        TreeMap<Integer, List<PianoSoundInstance>> instanceMap = uuidMap.computeIfAbsent(playerUUID, uuidK -> new TreeMap<>());
        instanceMap.computeIfAbsent(tone, k -> new ArrayList<>()).add(instance);
//        uuidMap.put(playerUUID, instanceMap);
//        this.soundMap.put(voicePos, uuidMap);
    }

    public @Nullable PianoSoundInstance removeFirst(BlockPos pos, UUID playerUUID, int tone) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> uuidMap = this.soundMap.get(pos);
        if (uuidMap == null || uuidMap.isEmpty()) return null;
        TreeMap<Integer, List<PianoSoundInstance>> instanceMap = uuidMap.get(playerUUID);
        if (instanceMap == null || instanceMap.isEmpty()) return null;
        if (instanceMap.get(tone) == null) return null;
        PianoSoundInstance old = null;
        if (!instanceMap.get(tone).isEmpty()) {
            old = instanceMap.get(tone).remove(0);
            if (instanceMap.get(tone).isEmpty()) {
                instanceMap.remove(tone);
                if (instanceMap.isEmpty()) {
                    uuidMap.remove(playerUUID);
                    if (uuidMap.isEmpty()) {
                        this.soundMap.remove(pos);
                    }
                }
            }
        }
        return old;
    }

    public PianoSoundInstance getFirst(BlockPos pos, UUID playerUUID, int tone) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> uuidMap = this.soundMap.get(pos);
        if (uuidMap == null || uuidMap.isEmpty()) return null;
        TreeMap<Integer, List<PianoSoundInstance>> instanceMap = uuidMap.get(playerUUID);
        if (instanceMap == null || instanceMap.isEmpty()) return null;
        List<PianoSoundInstance> instanceList = instanceMap.get(tone);
        if (instanceList == null) return null;
        return instanceList.get(0);
    }

    public @NotNull List<PianoSoundInstance> removeAll(BlockPos pos) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> removedUuidMap = this.soundMap.remove(pos);
        if (removedUuidMap == null || removedUuidMap.isEmpty()) return new ArrayList<>();

        List<PianoSoundInstance> instances = new ArrayList<>();
        for (TreeMap<Integer, List<PianoSoundInstance>> instanceMap : removedUuidMap.values()) {
            if (instanceMap.isEmpty()) continue;
            for (List<PianoSoundInstance> instanceList : instanceMap.values()) {
                instances.addAll(instanceList);
            }
        }
        removedUuidMap.clear();
        return instances;
    }

    /**
     * 摘走这台钢琴上所有“已经不需要再响”的实例，只把 {@code tones} 点名的那些留下。
     *
     * <p>{@code tones} 是玩家此刻还按着的音（{@code ModToneManager.getLastTones}），
     * 它们要继续响，也必须继续留在簿记里——等玩家真正抬手时再通过
     * {@code tryToStopPianoSound} -> {@code getFirst} 停掉。
     * 所以这里只能逐个音调摘，绝不可以把整个位置一起清空。</p>
     *
     * @return 移除的实例
     */
    public List<PianoSoundInstance> removeAllButLast(BlockPos pos, int[] tones) {
        if (ArrayUtils.isEmpty(tones)) return this.removeAll(pos);

        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> uuidMap = this.soundMap.get(pos);
        if (uuidMap == null || uuidMap.isEmpty()) return new ArrayList<>();

        List<Integer> toneList = Util.toIntegerList(tones);
        List<PianoSoundInstance> removedInstances = new ArrayList<>();
        Iterator<Map.Entry<UUID, TreeMap<Integer, List<PianoSoundInstance>>>> players = uuidMap.entrySet().iterator();
        while (players.hasNext()) {
            TreeMap<Integer, List<PianoSoundInstance>> instanceMap = players.next().getValue();
            Iterator<Map.Entry<Integer, List<PianoSoundInstance>>> notes = instanceMap.entrySet().iterator();
            while (notes.hasNext()) {
                Map.Entry<Integer, List<PianoSoundInstance>> note = notes.next();
                if (toneList.contains(note.getKey())) continue;
                removedInstances.addAll(note.getValue());
                notes.remove();
            }
            if (instanceMap.isEmpty()) players.remove();
        }
        if (uuidMap.isEmpty()) this.soundMap.remove(pos);
        return removedInstances;
    }

    public void clear() {
        this.soundMap.clear();
    }

    public static PianoSoundMap create() {
        return new PianoSoundMap();
    }
}
