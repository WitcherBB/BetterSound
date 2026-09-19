package com.witcherbb.bettersound;

import com.witcherbb.bettersound.client.util.PianoSoundMap;
import net.minecraft.core.BlockPos;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * “正在发声”的钢琴音实例索引表：按方块位置 -> 玩家 -> 音调 -> 实例列表分层。
 *
 * <p>PianoSoundInstance 的静态初始化会去取 Minecraft 单例，脱离客户端构造不出来；
 * 而这张表只把它当作不透明元素存放，所以这里统一用 {@code null} 占位，
 * 通过 removeAll 返回的条数来验证“谁被留下、谁被拿走”的簿记是否正确。</p>
 */
public class PianoSoundMapTest {

    private static final BlockPos PIANO = new BlockPos(4, 5, 6);
    private static final BlockPos OTHER_PIANO = new BlockPos(7, 8, 9);
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    private static void press(PianoSoundMap map, BlockPos pos, UUID player, int tone) {
        map.put(pos, player, tone, null);
    }

    @Test
    public void removeFirstTakesExactlyOneInstancePerCall() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, ALICE, 39);

        map.removeFirst(PIANO, ALICE, 39);
        assertEquals("三个进去拿走一个，还剩两个", 2, map.removeAll(PIANO).size());
    }

    @Test
    public void removeFirstOnUnknownKeysChangesNothing() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);

        assertNull(map.removeFirst(OTHER_PIANO, ALICE, 39));
        assertNull(map.removeFirst(PIANO, BOB, 39));
        assertNull(map.removeFirst(PIANO, ALICE, 40));

        assertEquals("不相干的查询不该动到已有记录", 1, map.removeAll(PIANO).size());
    }

    @Test
    public void playersAndTonesAreTrackedSeparately() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, ALICE, 43);
        press(map, PIANO, BOB, 39);

        map.removeFirst(PIANO, ALICE, 39);

        assertEquals("只该拿走 ALICE 的 39 号音，另外两个还在", 2, map.removeAll(PIANO).size());
    }

    @Test
    public void removeAllCollectsEverythingAtOnePosition() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, ALICE, 43);
        press(map, PIANO, BOB, 41);
        press(map, OTHER_PIANO, ALICE, 39);

        assertEquals(3, map.removeAll(PIANO).size());
        assertTrue("同一个位置再取一次应当是空的", map.removeAll(PIANO).isEmpty());
        assertEquals("别的钢琴不受影响", 1, map.removeAll(OTHER_PIANO).size());
    }

    @Test
    public void removeAllOnUnknownPositionIsEmpty() {
        assertTrue(PianoSoundMap.create().removeAll(PIANO).isEmpty());
    }

    @Test
    public void removeAllButLastKeepsTheExceptedTones() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, ALICE, 43);
        press(map, PIANO, BOB, 46);

        List<?> handedOver = map.removeAllButLast(PIANO, new int[]{39});

        assertEquals("只有 39 号音是被点名留下的，其余两个应当交出去停掉", 2, handedOver.size());
    }

    @Test
    public void removeAllButLastWithoutExceptedTonesBehavesLikeRemoveAll() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, ALICE, 43);
        press(map, PIANO, BOB, 46);

        assertEquals(3, map.removeAllButLast(PIANO, new int[0]).size());
    }

    @Test
    public void removeAllButLastOnUnknownPositionIsEmpty() {
        assertTrue(PianoSoundMap.create().removeAllButLast(PIANO, new int[]{39}).isEmpty());
    }

    @Test
    public void clearDropsEveryPositionAndLeavesTheMapUsable() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, OTHER_PIANO, BOB, 43);

        map.clear();

        assertTrue(map.removeAll(PIANO).isEmpty());
        assertTrue(map.removeAll(OTHER_PIANO).isEmpty());

        press(map, OTHER_PIANO, BOB, 43);
        assertEquals("清空之后还能照常记录", 1, map.removeAll(OTHER_PIANO).size());
    }

    /**
     * 被点名留下的音必须继续留在簿记里：它们还在响，等玩家真正抬手时
     * （release -> tryToStopPianoSound -> getFirst）才停得掉。
     * 若实现把整个位置一起清空，这些音就会脱离簿记，永远停不下来。
     */
    @Test
    public void exceptedTonesStayTrackedSoTheyCanStillBeStopped() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);   // 玩家还按着，点名留下
        press(map, PIANO, ALICE, 43);   // 已经放开，应当交出去

        assertEquals("只交出已经放开的 43 号音", 1, map.removeAllButLast(PIANO, new int[]{39}).size());
        assertEquals("留下的 39 号音必须还在表里", 1, map.removeAll(PIANO).size());
    }

    @Test
    public void nothingIsHandedOverWhenEveryToneIsStillHeld() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, PIANO, BOB, 43);

        assertTrue("所有音都被点名留下时不该交出任何实例",
                map.removeAllButLast(PIANO, new int[]{39, 43}).isEmpty());
        assertEquals("而且这些音必须原封不动地留在表里", 2, map.removeAll(PIANO).size());
    }

    /** 交完之后空掉的位置不能再留个空壳占着 map。 */
    @Test
    public void emptiedPositionStillAcceptsNewNotes() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 43);

        assertEquals(1, map.removeAllButLast(PIANO, new int[]{39}).size());
        assertTrue("交完之后这个位置应当没有任何在响的音", map.removeAll(PIANO).isEmpty());

        press(map, PIANO, ALICE, 39);
        assertEquals("同一台钢琴还能继续记录新按下的音", 1, map.removeAll(PIANO).size());
    }
}
