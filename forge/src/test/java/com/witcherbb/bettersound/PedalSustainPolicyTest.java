package com.witcherbb.bettersound;

import com.witcherbb.bettersound.music.SustainPolicy;
import com.witcherbb.bettersound.music.midi.PedalSustainPolicy;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 延音释放策略的单测（纯逻辑，不需要 Minecraft 运行环境）。
 * 这是 NBS 与 MIDI 自动播放之间唯一的实质差异，所以单独钉死。
 */
public class PedalSustainPolicyTest {

    @Test
    public void releasesOnlyWhenPedalGoesUp() {
        Map<Integer, Boolean> changes = new HashMap<>();
        changes.put(0, true);    // 游戏刻 0 踩下
        changes.put(20, false);  // 游戏刻 20 抬起

        PedalSustainPolicy policy = new PedalSustainPolicy(changes, 0);
        for (int tick = 0; tick < 20; tick++) {
            assertFalse("tick " + tick + " 踩着踏板，不应释放", policy.shouldRelease(tick));
        }
        assertTrue("踏板抬起时应当释放一次", policy.shouldRelease(20));
        for (int tick = 21; tick < 100; tick++) {
            assertFalse("tick " + tick + " 不应再释放", policy.shouldRelease(tick));
        }
    }

    @Test
    public void safetyValveForcesPeriodicReleaseWhenPedalStaysDown() {
        Map<Integer, Boolean> changes = new HashMap<>();
        changes.put(0, true);   // 整曲一直踩着不放

        PedalSustainPolicy policy = new PedalSustainPolicy(changes, 40);
        for (int tick = 0; tick <= 40; tick++) {
            assertFalse("tick " + tick + " 还不该触发安全阀", policy.shouldRelease(tick));
        }
        assertTrue("超过上限应当强制释放一次", policy.shouldRelease(41));
        assertFalse(policy.shouldRelease(42));
        assertTrue("释放之后重新计时", policy.shouldRelease(82));
    }

    @Test
    public void emptyPedalMapFallsBackToBarLines() {
        PedalSustainPolicy policy = new PedalSustainPolicy(Map.of(), 0);
        assertTrue("没有可用释放点时应交回小节线", policy.releaseOnBarLine());
        for (int tick = 0; tick < 200; tick++) {
            assertFalse(policy.shouldRelease(tick));
        }
    }

    @Test
    public void pedalTimelineTakesOverFromBarLines() {
        Map<Integer, Boolean> changes = new HashMap<>();
        changes.put(0, true);
        PedalSustainPolicy policy = new PedalSustainPolicy(changes, 0);
        assertFalse("有踏板时间轴时不再走小节线", policy.releaseOnBarLine());
    }

    /** 默认策略（NBS 用的那个）必须保持「不额外释放 + 走小节线」 */
    @Test
    public void barLinePolicyDoesNothingExtra() {
        SustainPolicy policy = SustainPolicy.BAR_LINE;
        assertTrue(policy.releaseOnBarLine());
        for (int tick = 0; tick < 200; tick++) {
            assertFalse(policy.shouldRelease(tick));
        }
    }
}
