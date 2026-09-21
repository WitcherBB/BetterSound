package com.witcherbb.bettersound;

import com.witcherbb.bettersound.common.utils.Util;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 装箱/拆箱与字节列表的小工具。这些方法散布在 NBT 同步与按键处理里，
 * 空数组和负数是最容易出错的两个边界。
 */
public class UtilTest {

    @Test
    public void intArrayToIntegerList() {
        assertEquals(List.of(1, 2, 3), Util.toIntegerList(new int[]{1, 2, 3}));
        assertEquals(List.of(-1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE),
                Util.toIntegerList(new int[]{-1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE}));
        assertTrue("空数组应当给出空列表", Util.toIntegerList(new int[0]).isEmpty());
    }

    @Test
    public void intArrayToIntegerArray() {
        assertArrayEquals(new Integer[]{1, 2, 3}, Util.toIntegerArray(new int[]{1, 2, 3}));
        assertArrayEquals(new Integer[0], Util.toIntegerArray(new int[0]));
    }

    @Test
    public void integerListToIntArray() {
        assertArrayEquals(new int[]{1, 2, 3}, Util.toIntArray(List.of(1, 2, 3)));
        assertArrayEquals(new int[0], Util.toIntArray(List.of()));
        assertArrayEquals(new int[]{7}, Util.toIntArray(new ArrayList<>(List.of(7))));
    }

    @Test
    public void intConversionsRoundTrip() {
        int[] original = {-128, -1, 0, 1, 127, Integer.MIN_VALUE, Integer.MAX_VALUE};
        assertArrayEquals(original, Util.toIntArray(Util.toIntegerList(original)));
        assertArrayEquals(original, Util.toIntArray(Arrays.asList(Util.toIntegerArray(original))));
    }

    @Test
    public void byteArrayToList() {
        assertEquals(List.of((byte) 1, (byte) -2, (byte) 127), Util.toList(new byte[]{1, -2, 127}));
        assertTrue(Util.toList(new byte[0]).isEmpty());
        // 返回值必须是可变的普通列表，调用方会往里塞元素
        List<Byte> list = Util.toList(new byte[]{1});
        list.add((byte) 2);
        assertEquals(2, list.size());
    }

    @Test
    public void statusValuesAreStable() {
        assertEquals(0, Util.Status.NONE.getvalue());
        assertEquals(1, Util.Status.FAIL.getvalue());
        assertEquals(1, Util.Status.NULL.getvalue());
        assertEquals(2, Util.Status.SUCCESS.getvalue());
        // FAIL 与 NULL 共用数值 1，用它们区分“失败”与“空输入”时只能靠枚举本身
        assertEquals(Util.Status.FAIL.getvalue(), Util.Status.NULL.getvalue());
    }
}
