package com.witcherbb.bettersound.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PianoUtil {
    private static final List<Integer> BLACKS;
    private static final String[] keyNames = new String[]{
            "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"
    };

    public static boolean isBlackey(int id) {
        return BLACKS.contains(id);
    }

    public static Component getKeyName(int id) {
        int length = keyNames.length;

        if (id < 0 || id >= 88) {
            return Component.literal("Invalid key ID");
        } else if (id < 3) {
            return switch (id) {
                case 0 -> Component.literal("A0");
                case 1 -> Component.literal("A#/Bb0");
                case 2 -> Component.literal("B0");
                default -> Component.empty();
            };
        }

        int effectiveId = (id - 3) % length;
        int depth = (id - 3) / length + 1;

        return Component.literal(keyNames[effectiveId] + depth);
    }

    static {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        int c = 3;
        for (int i = 0; i < 7; i++, c += 12) {
            int key = c + 1;
            list.addAll(List.of(key, key += 2, key += 3, key += 2, key + 2));
        }
        BLACKS = ImmutableList.copyOf(list);
    }
}
