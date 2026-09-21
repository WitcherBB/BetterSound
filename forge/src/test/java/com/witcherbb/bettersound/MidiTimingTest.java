package com.witcherbb.bettersound;

import com.witcherbb.bettersound.music.MusicTiming;
import com.witcherbb.bettersound.music.midi.MidiTiming;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MIDI 时间轴换算的纯函数测试（不依赖 Minecraft 运行环境）。
 */
public class MidiTimingTest {

    /**
     * 整个 MIDI 方案的地基：把 tempo 固定成 TEMPO_AS_GAME_TICKS 之后，
     * MusicTiming 的换算必须退化成恒等映射，音符表的键才能直接当成游戏刻用。
     */
    @Test
    public void tempoAsGameTicksMakesTimingIdentity() {
        for (int tick = 0; tick <= 20000; tick++) {
            assertEquals(tick, MusicTiming.toGameTick(tick, MidiTiming.TEMPO_AS_GAME_TICKS));
        }
    }

    @Test
    public void microsToGameTick() {
        assertEquals(0, MidiTiming.microsToGameTick(0L));
        assertEquals(0, MidiTiming.microsToGameTick(24_999L));   // 不足半刻
        assertEquals(1, MidiTiming.microsToGameTick(25_000L));   // 恰好半刻，向上取
        assertEquals(1, MidiTiming.microsToGameTick(50_000L));   // 1 刻 = 50ms
        assertEquals(20, MidiTiming.microsToGameTick(1_000_000L));
        assertEquals(40, MidiTiming.microsToGameTick(2_000_000L));
        // 10 分钟：仍应精确落在 20 刻/秒的网格上，不使用浮点
        assertEquals(12_000, MidiTiming.microsToGameTick(600_000_000L));
    }

    @Test
    public void pianoRangeCoversExactly88KeysFromA0ToC8() {
        assertEquals(88, MidiTiming.PIANO_KEY_COUNT);
        assertEquals(21, MidiTiming.PIANO_LOW_MIDI_NOTE);
        assertEquals(108, MidiTiming.PIANO_HIGH_MIDI_NOTE);

        assertTrue(MidiTiming.isInPianoRange(21));    // A0，最低键
        assertTrue(MidiTiming.isInPianoRange(108));   // C8，最高键
        assertFalse(MidiTiming.isInPianoRange(20));   // 比 A0 还低半音
        assertFalse(MidiTiming.isInPianoRange(109));  // 比 C8 还高半音

        assertEquals(0, MidiTiming.midiNoteToTone(21));    // A0
        assertEquals(3, MidiTiming.midiNoteToTone(24));    // C1
        assertEquals(39, MidiTiming.midiNoteToTone(60));   // C4 中央 C
        assertEquals(87, MidiTiming.midiNoteToTone(108));  // C8

        // 音域内的每个音高都应映射到 0..87
        for (int note = MidiTiming.PIANO_LOW_MIDI_NOTE; note <= MidiTiming.PIANO_HIGH_MIDI_NOTE; note++) {
            int tone = MidiTiming.midiNoteToTone(note);
            assertTrue("tone out of range: " + tone, tone >= 0 && tone < 88);
        }
    }

    @Test
    public void velocityToVolume() {
        assertEquals(100, MidiTiming.velocityToVolume(127));
        assertEquals(79, MidiTiming.velocityToVolume(100));
        assertEquals(50, MidiTiming.velocityToVolume(64));
        assertEquals(1, MidiTiming.velocityToVolume(1));
        assertEquals(1, MidiTiming.velocityToVolume(0));   // 0 力度不是 Note On，这里只做下限保护
        // 单调不减
        byte previous = 0;
        for (int velocity = 1; velocity <= 127; velocity++) {
            byte volume = MidiTiming.velocityToVolume(velocity);
            assertTrue(volume >= previous);
            assertTrue(volume >= 1 && volume <= 100);
            previous = volume;
        }
    }

    @Test
    public void barGameTicks() {
        // 120 BPM（500000 微秒/四分音符）下，一小节 2 秒 = 40 游戏刻
        assertEquals(40, MidiTiming.barGameTicks(4, 2, 480, 500_000L));
        assertEquals(30, MidiTiming.barGameTicks(3, 2, 480, 500_000L));   // 3/4
        assertEquals(30, MidiTiming.barGameTicks(6, 3, 480, 500_000L));   // 6/8 = 3 个四分音符
        // 60 BPM 下 4/4 一小节 4 秒 = 80 游戏刻
        assertEquals(80, MidiTiming.barGameTicks(4, 2, 480, 1_000_000L));
        // 非法/缺失拍号回退到 4/4
        assertEquals(40, MidiTiming.barGameTicks(0, 2, 480, 500_000L));
        assertEquals(40, MidiTiming.barGameTicks(4, 99, 480, 500_000L));
        // 结果至少为 1，避免上层取模除零
        assertEquals(1, MidiTiming.barGameTicks(1, 8, 480, 1L));
    }

    @Test
    public void tempoSegmentConversion() {
        MidiTiming.TempoSegment first = new MidiTiming.TempoSegment(0L, 0L, 500_000L);
        assertEquals(0L, MidiTiming.toMicros(0L, first, 480));
        assertEquals(500_000L, MidiTiming.toMicros(480L, first, 480));      // 一个四分音符
        assertEquals(1_000_000L, MidiTiming.toMicros(960L, first, 480));    // 两个四分音符

        // 从 tick 960 起速度翻倍到 1000000 微秒/四分音符，此时已累计 1000000 微秒
        MidiTiming.TempoSegment second = new MidiTiming.TempoSegment(960L, 1_000_000L, 1_000_000L);
        assertEquals(1_000_000L, MidiTiming.toMicros(960L, second, 480));
        assertEquals(2_000_000L, MidiTiming.toMicros(1440L, second, 480));
    }

    @Test
    public void smpteConversion() {
        assertEquals(1000, MidiTiming.smpteTicksPerSecond(25.0F, 40));
        assertEquals(1200, MidiTiming.smpteTicksPerSecond(30.0F, 40));
        // 1000 tick/秒时，1 个 tick 就是 1 毫秒
        assertEquals(1_000L, MidiTiming.smpteToMicros(1L, 1000));
        assertEquals(1_000_000L, MidiTiming.smpteToMicros(1000L, 1000));
        assertEquals(2_000_000L, MidiTiming.smpteToMicros(2000L, 1000));
    }
}
