package com.witcherbb.test;

import cpw.mods.bootstraplauncher.BootstrapLauncher;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainTest {
    @Test
    public void test() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        list.remove(0);
        list.remove(0);
        System.out.println(list);
    }

    public int getZero(float num, int start, float offset) {
        if (offset < num) {
            return getZero(num, start + 1, offset + 1.0F / (start * start));
        } else {
            return start;
        }
    }

    public static class Pojo implements Comparable<Pojo> {
        private int x;

        public Pojo(int x) {
            this.x = x;
        }

        @Override
        public int compareTo(@NotNull MainTest.Pojo o) {
            if (this.x == o.x) return 0;
            else if (this.x < o.x) return -1;
            return 1;
        }

        @Override
        public String toString() {
            return "Pojo{" +
                    "x=" + x +
                    '}';
        }
    }
}
