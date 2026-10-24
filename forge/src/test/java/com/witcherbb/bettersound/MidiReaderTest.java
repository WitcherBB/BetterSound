package com.witcherbb.bettersound;

import com.witcherbb.bettersound.exception.FileIsNotMidiException;
import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
import com.witcherbb.bettersound.music.midi.MidiReader;
import com.witcherbb.bettersound.music.midi.MidiTiming;
import com.witcherbb.bettersound.music.midi.bean.MidiSong;
import org.junit.Test;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 端到端：程序化生成一个真实的 MIDI 文件，交给 {@link MidiReader} 解析并编译，
 * 覆盖越界音符剔除、打击乐通道剔除、曲名挑选、CC64 踏板编译与游戏刻折算。
 */
public class MidiReaderTest {
    private static final int TPQN = 480;
    private static final int META_TRACK_NAME = 0x03;
    private static final int META_TEMPO = 0x51;
    private static final int META_TIME_SIGNATURE = 0x58;

    /** 120 BPM = 500000 微秒/四分音符，4/4，则一小节 2 秒 = 40 游戏刻 */
    private static final byte[] TEMPO_120_BPM = {0x07, (byte) 0xA1, 0x20};

    @Test
    public void parsesGeneratedMidiEndToEnd() throws Exception {
        File file = this.generateMidi();

        MidiSong song;
        try (MidiReader reader = new MidiReader(file)) {
            song = reader.readSong();
        }

        // --- 解析阶段：越界与打击乐在进入歌曲前就被剔除 ---
        assertEquals(TPQN, song.ticksPerQuarter);
        assertEquals(0, song.smpteTicksPerSecond);
        assertEquals(3, song.totalNotes);
        assertEquals(1, song.outOfRangeNotes);   // MIDI 12 比 A0 还低
        assertEquals(1, song.drumNotes);         // 打击乐通道 9
        assertEquals(1, song.notes.size());      // 只剩 MIDI 60（C4）
        assertEquals("DSH Test Song", song.name); // 取音符最多的轨道名，而不是指挥轨的 "Conductor"
        assertEquals(40, song.barGameTicks);     // 4/4 @120BPM

        // --- 编译阶段：音符折算到游戏刻 ---
        PianoSongTrack track = song.compile(0);
        assertEquals((short) MidiTiming.TEMPO_AS_GAME_TICKS, track.tempo());
        assertEquals(40, track.subsectionLength());

        List<Note> atTickZero = track.getNotes(0);
        assertNotNull(atTickZero);
        assertEquals("游戏刻 0：踏板踩下 + 那个音符", 2, atTickZero.size());
        assertTrue("踏板事件排在最前面", atTickZero.get(0).isPedalDown());
        assertTrue(atTickZero.get(1).isPlay());
        assertEquals(39, atTickZero.get(1).getPitch());   // C4 = MIDI 60 -> tone 39
        assertEquals(100, atTickZero.get(1).getVolume()); // velocity 127 -> 100

        // --- 编译阶段：释放点由"音符时值 / 小节末"折成逐音哨兵 ---
        assertFalse("MIDI 的释放点全部来自事件表，不再走小节线", track.releaseOnBarLine());

        // --- 踏板跳变本身也要有事件，否则模型不会跟着变 ---
        List<Note> pedalUpTick = track.getNotes(20);
        assertNotNull("抬踏板那一刻要有踏板事件", pedalUpTick);
        assertTrue(pedalUpTick.get(0).isPedalUp());
        assertFalse("抬踏板不该再整片切", pedalUpTick.get(0).isRelease());
        // 这个音没有配对的 Note Off -> 当作按到本小节末尾（4/4 @120BPM，一小节 40 游戏刻）
        List<Note> releaseAt40 = track.getNotes(40);
        assertNotNull(releaseAt40);
        assertEquals(1, releaseAt40.size());
        assertTrue("应当是逐音释放而不是整片释放", releaseAt40.get(0).isReleaseTone());
        assertEquals(39, releaseAt40.get(0).getPitch());   // C4 = tone 39
    }

    @Test
    public void rejectsNonMidiExtension() {
        try {
            new MidiReader(new File("build/dsh-verify/not-a-midi.txt"));
            throw new AssertionError("应当因为扩展名不对而抛异常");
        } catch (FileIsNotMidiException expected) {
            // 符合预期
        }
    }

    /** 没有踏板信息（整首没有 CC64）时，踏板视为从没踩过，音符按自己的时值 / 小节末松开 */
    @Test
    public void pedalWithoutAnyPressFallsBackToBarLines() {
        MidiSong song = new MidiSong("no-press.mid");
        song.ticksPerQuarter = TPQN;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, MidiTiming.DEFAULT_MICROS_PER_QUARTER));
        song.notes.add(new MidiSong.RawNote(0L, 60, 100, 0));
        song.contributingChannels.add(0);
        song.pedals.add(new MidiSong.RawPedal(0L, false, 0));

        PianoSongTrack track = song.compile(0);
        assertEquals("这一刻只有那个音符，不该多出释放事件", 1, track.getNotes(0).size());
        assertTrue(track.getNotes(0).get(0).isPlay());
        // 没有 Note Off -> 按到本小节末（默认 40 游戏刻）
        assertNotNull("没有踏板信息也照样按小节末松开", track.getNotes(40));
        assertTrue(track.getNotes(40).get(0).isReleaseTone());
    }

    /**
     * 多通道踏板取 OR：只要还有通道踩着，音就不能在别的通道抬脚时被松开，
     * 它的释放点要顺延到最后一个通道也抬脚的那一刻。
     */
    @Test
    public void pedalMergesChannelsWithOr() {
        MidiSong song = new MidiSong("multi-channel.mid");
        song.ticksPerQuarter = TPQN;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, MidiTiming.DEFAULT_MICROS_PER_QUARTER));
        // 时值 720 tick（= 游戏刻 15），本该在 15 松开
        song.notes.add(new MidiSong.RawNote(0L, 60, 100, 0, 720L));
        song.contributingChannels.add(0);
        song.contributingChannels.add(1);

        song.pedals.add(new MidiSong.RawPedal(0L, true, 0));
        song.pedals.add(new MidiSong.RawPedal(0L, true, 1));
        song.pedals.add(new MidiSong.RawPedal(480L, false, 0));   // 通道 0 抬脚（游戏刻 10），但通道 1 还踩着
        song.pedals.add(new MidiSong.RawPedal(960L, false, 1));   // 通道 1 也抬脚（游戏刻 20），此时才真正松开

        PianoSongTrack track = song.compile(0);
        assertNull("通道 0 单独抬脚时不该松开", track.getNotes(15));
        List<Note> atRelease = track.getNotes(20);
        assertNotNull(atRelease);
        assertEquals("这一刻：踏板抬起 + 松开那个音", 2, atRelease.size());
        assertTrue(atRelease.get(0).isPedalUp());
        assertTrue(atRelease.get(1).isReleaseTone());
        assertEquals(39, atRelease.get(1).getPitch());
    }

    /** 没有任何音符的通道不参与踏板 OR 合并 */
    @Test
    public void pedalOnSilentChannelIsIgnored() {
        MidiSong song = new MidiSong("silent-channel.mid");
        song.ticksPerQuarter = TPQN;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, MidiTiming.DEFAULT_MICROS_PER_QUARTER));
        song.notes.add(new MidiSong.RawNote(0L, 60, 100, 0));
        song.contributingChannels.add(0);
        song.pedals.add(new MidiSong.RawPedal(0L, true, 5));   // 通道 5 没有任何音符

        // 通道 5 的踏板不参与合并 -> 踏板时间轴为空 -> 安全阀根本不启用（上限 40 刻也不会插释放）
        assertNull("通道 5 的踏板不该参与进来", song.compile(40).getNotes(41));
    }

    /**
     * 安全阀：曲子里踏板一直踩着不放时，每超过上限就强制释放一次——
     * 这条规则原本跑在服务端的每一个游戏刻上，现在挪到编译期插成事件。
     */
    @Test
    public void safetyValveInsertsForcedReleases() {
        MidiSong song = new MidiSong("stuck-pedal.mid");
        song.ticksPerQuarter = TPQN;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, MidiTiming.DEFAULT_MICROS_PER_QUARTER));
        song.barGameTicks = 40;
        song.notes.add(new MidiSong.RawNote(0L, 60, 100, 0));
        song.notes.add(new MidiSong.RawNote(4800L, 60, 100, 0));   // 5 秒处再来一个音，把曲子拉长到游戏刻 100
        song.contributingChannels.add(0);
        song.pedals.add(new MidiSong.RawPedal(0L, true, 0));       // 踩下之后再也不抬

        PianoSongTrack track = song.compile(40);   // 上限 40 游戏刻

        assertFalse(track.releaseOnBarLine());
        assertNull("还没到上限，不该插入释放", track.getNotes(40));
        assertTrue(track.getNotes(41).get(0).isReleaseAll());
        assertTrue(track.getNotes(82).get(0).isReleaseAll());
        assertNull("释放之后重新计时", track.getNotes(83));
    }

    /**
     * 同一刻既松键又起新音时，释放事件必须排在该刻事件表的最前面。
     * <p>引擎靠"攒完再播"也能保证这个顺序，这里额外把顺序钉在数据上——
     * 同音重复（松掉 X 再重弹 X）时一旦反了，就会把刚起的新音切掉。
     */
    @Test
    public void releaseComesFirstWhenAKeyIsReleasedOnTheSameTickAsANote() {
        MidiSong song = new MidiSong("same-tick.mid");
        song.ticksPerQuarter = TPQN;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, MidiTiming.DEFAULT_MICROS_PER_QUARTER));
        song.barGameTicks = 40;
        song.notes.add(new MidiSong.RawNote(0L, 60, 100, 0));      // 没有 Note Off -> 释放点 = 小节末 40
        song.notes.add(new MidiSong.RawNote(1920L, 64, 100, 0));   // 正好在小节线上起新音（游戏刻 40）
        song.contributingChannels.add(0);

        PianoSongTrack track = song.compile(0);

        List<Note> sameTick = track.getNotes(40);
        assertNotNull(sameTick);
        assertEquals("这一刻应当既有释放也有新音", 2, sameTick.size());
        assertTrue("释放必须排在演奏之前", sameTick.get(0).isReleaseTone());
        assertEquals(39, sameTick.get(0).getPitch());
        assertTrue(sameTick.get(1).isPlay());
        assertEquals(43, sameTick.get(1).getPitch());   // E4 = MIDI 64 -> tone 43
    }

    /**
     * 读取琴键按住的时长：Note Off（含力度为 0 的 Note On）与 Note On 配对，
     * 同音高重叠时按先后顺序配对；配不上的保持未知。
     */
    @Test
    public void readsNoteDurationFromNoteOff() throws Exception {
        File file = this.generateDurationMidi();

        MidiSong song;
        try (MidiReader reader = new MidiReader(file)) {
            song = reader.readSong();
        }

        assertEquals(4, song.totalNotes);
        assertEquals(4, song.notes.size());

        // C4 响了两遍并且重叠，两个 Note Off 按先后顺序各配一个
        MidiSong.RawNote first = song.notes.get(0);
        assertEquals(0L, first.tick());
        assertEquals(480L, first.durationTicks());
        assertTrue(first.hasDuration());

        MidiSong.RawNote second = song.notes.get(1);
        assertEquals(100L, second.tick());
        assertEquals(500L, second.durationTicks());

        // 力度为 0 的 Note On 与 Note Off 等价
        MidiSong.RawNote third = song.notes.get(2);
        assertEquals(960L, third.tick());
        assertEquals(240L, third.durationTicks());

        // 没有 Note Off 的音符保持未知，不替它猜一个数
        MidiSong.RawNote unclosed = song.notes.get(3);
        assertEquals(1440L, unclosed.tick());
        assertEquals(MidiSong.DURATION_UNKNOWN, unclosed.durationTicks());
        assertFalse(unclosed.hasDuration());

        // 时长可以直接推出结束时刻，再交给速度表换算：480 tick = 一个四分音符 = 0.5 秒
        assertEquals(500_000L, song.toMicros(first.tick() + first.durationTicks()));

        // 编译后落到业务音符上，单位换算成游戏刻：0.5 秒 = 10 游戏刻
        PianoSongTrack compiled = song.compile(0);
        List<Note> atStart = compiled.getNotes(0);
        assertNotNull(atStart);
        assertEquals(10, atStart.get(0).getDuration());
        assertTrue(atStart.get(0).hasDuration());

        // 没闭合的那个音符（1440 tick = 1.5 秒 = 游戏刻 30）落到业务音符上仍然是未知
        List<Note> unclosedNote = compiled.getNotes(30);
        assertNotNull(unclosedNote);
        assertEquals(Note.DURATION_UNKNOWN, unclosedNote.get(0).getDuration());
        assertFalse(unclosedNote.get(0).hasDuration());
    }

    /** 生成一份专门验证时长的 MIDI：重叠同音、力度 0 的 Note On、以及一个不闭合的音符 */
    private File generateDurationMidi() throws Exception {
        File dir = new File("build/dsh-verify");
        assertTrue(dir.exists() || dir.mkdirs());
        File file = new File(dir, "dsh-duration-test.mid");

        Sequence sequence = new Sequence(Sequence.PPQ, TPQN);
        Track track = sequence.createTrack();
        track.add(new MidiEvent(this.meta(META_TRACK_NAME, "Duration Test"), 0L));
        track.add(new MidiEvent(this.meta(META_TEMPO, TEMPO_120_BPM), 0L));

        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), 0L));
        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), 100L));   // 同音高重叠
        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), 480L));
        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), 600L));

        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, 100), 960L));
        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, 0), 1200L));    // 力度 0 = Note Off

        track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, 100), 1440L));  // 一直不闭合

        MidiSystem.write(sequence, 1, file);
        return file;
    }

    /** 生成一个 Format 1 的 MIDI：轨道 0 只放速度/拍号（名字是 Conductor），轨道 1 才是真正的演奏 */
    private File generateMidi() throws Exception {
        File dir = new File("build/dsh-verify");
        assertTrue(dir.exists() || dir.mkdirs());
        File file = new File(dir, "dsh-generated-test.mid");

        Sequence sequence = new Sequence(Sequence.PPQ, TPQN);

        Track conductor = sequence.createTrack();
        conductor.add(new MidiEvent(this.meta(META_TRACK_NAME, "Conductor"), 0L));
        conductor.add(new MidiEvent(this.meta(META_TEMPO, TEMPO_120_BPM), 0L));
        conductor.add(new MidiEvent(this.meta(META_TIME_SIGNATURE, new byte[]{4, 2, 24, 8}), 0L));

        Track piano = sequence.createTrack();
        piano.add(new MidiEvent(this.meta(META_TRACK_NAME, "DSH Test Song"), 0L));
        piano.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 127), 0L));    // 音域内 C4
        piano.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 12, 100), 0L));    // 越界，应丢弃
        piano.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 9, 38, 100), 0L));    // 打击乐通道，应丢弃
        piano.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 64, 127), 0L));   // 踏板踩下
        piano.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 64, 0), 960L));   // 1 秒后抬起

        MidiSystem.write(sequence, 1, file);
        return file;
    }

    private MetaMessage meta(int type, String value) throws Exception {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        return new MetaMessage(type, data, data.length);
    }

    private MetaMessage meta(int type, byte[] data) throws Exception {
        return new MetaMessage(type, data, data.length);
    }
}
