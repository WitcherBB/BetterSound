package com.witcherbb.bettersound;

import com.witcherbb.bettersound.client.resources.sounds.PianoSoundInstance;
import com.witcherbb.bettersound.client.util.PianoSoundMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * “正在发声”的钢琴音索引表：方块位置 -> 玩家 -> 音调 -> 声音实例。
 *
 * <p>表里存的就是真实的 {@link PianoSoundInstance}，构造它本身并不需要客户端，
 * 只需要一个 {@link SoundEvent}；而 SoundEvent 的静态初始化会一路走到 BuiltInRegistries，
 * 所以动手之前得先把注册表 bootstrap 起来（见 {@link #bootstrapRegistries()}）。
 * 既然拿得到真实实例，断言就直接比对对象本身，而不是像以前那样用 null 占位再数条数。</p>
 *
 * <p>每个音调只有一个槽位：同一个音重复按下时后来的覆盖先前的（与
 * {@code ModSoundManager.playPianoSound} 的实际行为一致）。</p>
 */
public class PianoSoundMapTest {

    private static final BlockPos PIANO = new BlockPos(4, 5, 6);
    private static final BlockPos OTHER_PIANO = new BlockPos(7, 8, 9);
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    private static SoundEvent noteSound;

    /**
     * {@code SoundEvent} 的静态初始化会触发 {@code BuiltInRegistries}，而 MappedRegistry
     * 的构造器带 “Not bootstrapped” 检查，不先建注册表连 new 一个 SoundEvent 都会炸。
     */
    @BeforeClass
    public static void bootstrapRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        noteSound = SoundEvent.createVariableRangeEvent(new ResourceLocation("bettersound", "test_note"));
    }

    /** 按下一个音：登记的是真实的声音实例，和 ModSoundManager.playPianoSound 走的是同一条路。 */
    private static PianoSoundInstance press(PianoSoundMap map, BlockPos pos, UUID player, int tone) {
        PianoSoundInstance instance = PianoSoundInstance.forBlock(noteSound, pos, player, tone, false);
        map.put(pos, player, tone, instance);
        return instance;
    }

    @Test
    public void pressedNoteIsRetrievableByIdentity() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance instance = press(map, PIANO, ALICE, 39);

        assertSame("取回来的必须就是按下去的那个实例", instance, map.get(PIANO, ALICE, 39));
        assertNull("没按过的音是空的", map.get(PIANO, ALICE, 43));
        assertNull("别的钢琴上没有这个音", map.get(OTHER_PIANO, ALICE, 39));
        assertNull("别的玩家没有按这个音", map.get(PIANO, BOB, 39));
    }

    @Test
    public void pressingTheSameToneAgainKeepsTheNewestInstance() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        PianoSoundInstance newest = press(map, PIANO, ALICE, 39);

        assertSame("同一个音只留最新的那份", newest, map.remove(PIANO, ALICE, 39));
        assertTrue("拿走之后这个位置就不该再有在响的音", map.remove(PIANO).isEmpty());
    }

    @Test
    public void removeReturnsTheInstanceAndClearsTheSlot() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance instance = press(map, PIANO, ALICE, 39);

        assertSame(instance, map.remove(PIANO, ALICE, 39));
        assertNull(map.get(PIANO, ALICE, 39));
    }

    @Test
    public void removeOnUnknownKeysChangesNothing() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance instance = press(map, PIANO, ALICE, 39);

        assertNull(map.remove(OTHER_PIANO, ALICE, 39));
        assertNull(map.remove(PIANO, BOB, 39));
        assertNull(map.remove(PIANO, ALICE, 40));

        assertSame("不相干的查询不该动到已有记录", instance, map.get(PIANO, ALICE, 39));
    }

    @Test
    public void playersAndTonesAreTrackedSeparately() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance alice39 = press(map, PIANO, ALICE, 39);
        PianoSoundInstance alice43 = press(map, PIANO, ALICE, 43);
        PianoSoundInstance bob39 = press(map, PIANO, BOB, 39);

        assertSame(alice39, map.remove(PIANO, ALICE, 39));
        assertSame("只该拿走 ALICE 的 39 号音", alice43, map.get(PIANO, ALICE, 43));
        assertSame(bob39, map.get(PIANO, BOB, 39));
    }

    @Test
    public void removeCollectsEverySoundingInstanceAtOnePosition() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance alice39 = press(map, PIANO, ALICE, 39);
        PianoSoundInstance alice43 = press(map, PIANO, ALICE, 43);
        PianoSoundInstance bob41 = press(map, PIANO, BOB, 41);
        PianoSoundInstance otherPiano = press(map, OTHER_PIANO, ALICE, 39);

        List<PianoSoundInstance> removed = map.remove(PIANO);

        assertEquals("没在响的空槽不该被当成实例交出去", 3, removed.size());
        assertTrue(removed.contains(alice39));
        assertTrue(removed.contains(alice43));
        assertTrue(removed.contains(bob41));
        assertTrue("同一个位置再取一次应当是空的", map.remove(PIANO).isEmpty());
        assertEquals("别的钢琴不受影响", List.of(otherPiano), map.remove(OTHER_PIANO));
    }

    @Test
    public void removeOnUnknownPositionIsEmpty() {
        assertTrue(PianoSoundMap.create().remove(PIANO).isEmpty());
    }

    /**
     * 被点名留下的音必须继续留在簿记里：它们还在响，等玩家真正抬手时
     * （release -> tryToStopPianoSound -> get）才停得掉。
     */
    @Test
    public void removeButLastKeepsTheExceptedTones() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance held39 = press(map, PIANO, ALICE, 39);
        PianoSoundInstance released43 = press(map, PIANO, ALICE, 43);
        PianoSoundInstance released46 = press(map, PIANO, BOB, 46);

        List<PianoSoundInstance> handedOver = map.removeButLast(PIANO, new int[]{39});

        assertEquals("只有 39 号音是被点名留下的，其余两个应当交出去停掉", 2, handedOver.size());
        assertTrue(handedOver.contains(released43));
        assertTrue(handedOver.contains(released46));
        assertSame("留下的 39 号音必须还在表里", held39, map.get(PIANO, ALICE, 39));
    }

    @Test
    public void removeButLastWithoutExceptedTonesBehavesLikeRemove() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance alice39 = press(map, PIANO, ALICE, 39);
        PianoSoundInstance alice43 = press(map, PIANO, ALICE, 43);
        PianoSoundInstance bob46 = press(map, PIANO, BOB, 46);

        List<PianoSoundInstance> removed = map.removeButLast(PIANO, new int[0]);

        assertEquals(3, removed.size());
        assertTrue(removed.contains(alice39));
        assertTrue(removed.contains(alice43));
        assertTrue(removed.contains(bob46));
        assertTrue(map.remove(PIANO).isEmpty());
    }

    @Test
    public void removeButLastOnUnknownPositionIsEmpty() {
        assertTrue(PianoSoundMap.create().removeButLast(PIANO, new int[]{39}).isEmpty());
    }

    @Test
    public void nothingIsHandedOverWhenEveryToneIsStillHeld() {
        PianoSoundMap map = PianoSoundMap.create();
        PianoSoundInstance alice39 = press(map, PIANO, ALICE, 39);
        PianoSoundInstance bob43 = press(map, PIANO, BOB, 43);

        assertTrue("所有音都被点名留下时不该交出任何实例",
                map.removeButLast(PIANO, new int[]{39, 43}).isEmpty());
        assertSame(alice39, map.get(PIANO, ALICE, 39));
        assertSame(bob43, map.get(PIANO, BOB, 43));
    }

    @Test
    public void emptiedPositionStillAcceptsNewNotes() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 43);

        assertEquals(1, map.removeButLast(PIANO, new int[]{39}).size());
        assertTrue("交完之后这个位置应当没有任何在响的音", map.remove(PIANO).isEmpty());

        PianoSoundInstance again = press(map, PIANO, ALICE, 39);
        assertSame("同一台钢琴还能继续记录新按下的音", again, map.get(PIANO, ALICE, 39));
    }

    @Test
    public void clearDropsEveryPositionAndLeavesTheMapUsable() {
        PianoSoundMap map = PianoSoundMap.create();
        press(map, PIANO, ALICE, 39);
        press(map, OTHER_PIANO, BOB, 43);

        map.clear();

        assertNull(map.get(PIANO, ALICE, 39));
        assertTrue(map.remove(PIANO).isEmpty());
        assertTrue(map.remove(OTHER_PIANO).isEmpty());

        PianoSoundInstance again = press(map, OTHER_PIANO, BOB, 43);
        assertSame("清空之后还能照常记录", again, map.get(OTHER_PIANO, BOB, 43));
    }
}
