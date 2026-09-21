package com.witcherbb.bettersound;

import com.witcherbb.bettersound.common.utils.LastToneMap;
import net.minecraft.core.BlockPos;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 每台钢琴上每位玩家“当前还按着”的音符集合，延音与停止逻辑都依赖它。
 * 这里除了正常增删，重点盯住“条目清空后要连带把外层 map 也删掉”，
 * 否则长期游玩会在服务端攒下一堆空的 BlockPos 条目。
 */
public class LastToneMapTest {

    private static final BlockPos PIANO = new BlockPos(10, 64, 20);
    private static final BlockPos OTHER_PIANO = new BlockPos(-3, 70, 8);
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @Test
    public void newMapIsEmpty() {
        LastToneMap map = LastToneMap.create();
        assertTrue(map.isEmpty());
        assertArrayEquals("未知方块位置应当给出空数组", new int[0], map.getByPos(PIANO));
    }

    @Test
    public void keepsEveryNoteAPlayerIsHolding() {
        LastToneMap map = LastToneMap.create();
        map.put(PIANO, ALICE, 39);
        map.put(PIANO, ALICE, 43);
        map.put(PIANO, ALICE, 46);
        assertArrayEquals(new int[]{39, 43, 46}, map.getByPos(PIANO));
    }

    @Test
    public void aggregatesAllPlayersOnTheSamePiano() {
        LastToneMap map = LastToneMap.create();
        map.put(PIANO, ALICE, 10);
        map.put(PIANO, BOB, 20);
        map.put(PIANO, BOB, 21);

        int[] tones = map.getByPos(PIANO);
        Arrays.sort(tones);
        // 跨玩家聚合时顺序取决于 HashMap 的遍历顺序，只保证集合内容
        assertArrayEquals(new int[]{10, 20, 21}, tones);
    }

    @Test
    public void pianosAreIndependent() {
        LastToneMap map = LastToneMap.create();
        map.put(PIANO, ALICE, 1);
        map.put(OTHER_PIANO, ALICE, 2);
        assertArrayEquals(new int[]{1}, map.getByPos(PIANO));
        assertArrayEquals(new int[]{2}, map.getByPos(OTHER_PIANO));
    }

    @Test
    public void removeDropsExactlyOneOccurrence() {
        LastToneMap map = LastToneMap.create();
        // 同一个音被重复按下（前一次还没抬手），当前实现是有几条记几条
        map.put(PIANO, ALICE, 39);
        map.put(PIANO, ALICE, 39);

        assertTrue(map.remove(PIANO, ALICE, 39));
        assertArrayEquals("只该掉一条", new int[]{39}, map.getByPos(PIANO));

        assertTrue(map.remove(PIANO, ALICE, 39));
        assertTrue("最后一条被清掉后，整张表应当回到空的状态", map.isEmpty());
        assertArrayEquals(new int[0], map.getByPos(PIANO));
    }

    @Test
    public void removeReportsMissingEntries() {
        LastToneMap map = LastToneMap.create();
        assertFalse("位置未知", map.remove(PIANO, ALICE, 39));

        map.put(PIANO, ALICE, 39);
        assertFalse("玩家未知", map.remove(PIANO, BOB, 39));
        assertFalse("钢琴未知", map.remove(OTHER_PIANO, ALICE, 39));
        assertFalse("音符未知", map.remove(PIANO, ALICE, 40));
        assertArrayEquals("失败的删除不能影响已有记录", new int[]{39}, map.getByPos(PIANO));
    }

    @Test
    public void emptyEntriesArePrunedAllTheWayUp() {
        LastToneMap map = LastToneMap.create();
        map.put(PIANO, ALICE, 1);
        map.put(PIANO, BOB, 2);
        map.put(OTHER_PIANO, ALICE, 3);
        assertEquals(2, map.size());

        assertTrue(map.remove(PIANO, ALICE, 1));
        assertEquals("同一台钢琴上还有 BOB 的音，条目要留着", 2, map.size());
        assertArrayEquals(new int[]{2}, map.getByPos(PIANO));

        assertTrue(map.remove(PIANO, BOB, 2));
        assertEquals("这台钢琴已经没人按着了，条目应当被删掉", 1, map.size());
        assertFalse(map.containsKey(PIANO));
        assertTrue(map.containsKey(OTHER_PIANO));

        assertTrue(map.remove(OTHER_PIANO, ALICE, 3));
        assertTrue(map.isEmpty());
    }
}
