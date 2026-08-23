package com.witcherbb.bettersound.common.utils;

import com.witcherbb.bettersound.mixins.extenders.MinecraftServerExtender;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    public static List<Integer> toIntegerList(int[] nums) {
        return Arrays.stream(nums).boxed().toList();
    }

    public static Integer[] toIntegerArray(int[] nums) {
        return Arrays.stream(nums).boxed().toArray(Integer[]::new);
    }

    public static int[] toIntArray(@NotNull List<Integer> nums) {
        return ArrayUtils.toPrimitive(nums.toArray(Integer[]::new));
    }

    public static List<Byte> toList(byte[] bytes) {
        List<Byte> list = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            list.add(bytes[i]);
        }
        return list;
    }

    public enum Status {
        NONE(0),
        FAIL(1),
        NULL(1),
        SUCCESS(2);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getvalue() {
            return this.value;
        }
    }
}
