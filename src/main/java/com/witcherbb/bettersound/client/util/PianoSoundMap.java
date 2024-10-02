package com.witcherbb.bettersound.client.util;

import com.witcherbb.bettersound.client.resources.sounds.PianoSoundInstance;
import com.witcherbb.bettersound.common.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class PianoSoundMap extends HashMap<BlockPos, HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>>> {

    public void put(BlockPos pos, UUID playerUUID, int tone, PianoSoundInstance instance) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> uuidMap = this.computeIfAbsent(pos, posK -> new HashMap<>());
        TreeMap<Integer, List<PianoSoundInstance>> instanceMap = uuidMap.computeIfAbsent(playerUUID, uuidK -> new TreeMap<>());
        instanceMap.computeIfAbsent(tone, k -> new ArrayList<>()).add(instance);
//        uuidMap.put(playerUUID, instanceMap);
//        this.put(pos, uuidMap);
    }

    public @Nullable PianoSoundInstance removeFirst(BlockPos pos, UUID playerUUID, int tone) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> uuidMap = this.get(pos);
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
                        this.remove(pos);
                    }
                }
            }
        }
        return old;
    }

    public PianoSoundInstance get(BlockPos pos, UUID playerUUID, int tone) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> uuidMap = this.get(pos);
        if (uuidMap == null || uuidMap.isEmpty()) return null;
        TreeMap<Integer, List<PianoSoundInstance>> instanceMap = uuidMap.get(playerUUID);
        if (instanceMap == null || instanceMap.isEmpty()) return null;
        List<PianoSoundInstance> instanceList = instanceMap.get(tone);
        if (instanceList == null) return null;
        return instanceList.get(0);
    }

    public @NotNull List<PianoSoundInstance> removeAll(BlockPos pos) {
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> removedUuidMap = this.remove(pos);
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

    public List<PianoSoundInstance> removeAllButLast(BlockPos pos, int[] tones) {
        if (tones.length == 0) return this.removeAll(pos);

        List<Integer> toneList = Util.toIntegerList(tones);
        HashMap<UUID, TreeMap<Integer, List<PianoSoundInstance>>> removedUuidMap = this.remove(pos);
        if (removedUuidMap == null || removedUuidMap.isEmpty()) return new ArrayList<>();

        List<PianoSoundInstance> instances = new ArrayList<>();
        for (TreeMap<Integer, List<PianoSoundInstance>> instanceMap : removedUuidMap.values()) {
            if (instanceMap.isEmpty()) continue;
            for (Entry<Integer, List<PianoSoundInstance>> instanceEntry : instanceMap.entrySet()) {
                if (toneList.contains(instanceEntry.getKey())) continue;
                instances.addAll(instanceEntry.getValue());
            }
        }
        removedUuidMap.clear();
        return instances;
    }

    public static PianoSoundMap create() {
        return new PianoSoundMap();
    }
}
