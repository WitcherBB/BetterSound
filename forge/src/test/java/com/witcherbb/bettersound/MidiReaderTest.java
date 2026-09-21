package com.witcherbb.bettersound;

import com.witcherbb.bettersound.exception.FileIsNotMidiException;
import com.witcherbb.bettersound.music.bean.Note;
import com.witcherbb.bettersound.music.bean.PianoSongTrack;
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
import static org.junit.Assert.assertNotNull;
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
        PianoSongTrack track = song.compile();
        assertEquals((short) MidiTiming.TEMPO_AS_GAME_TICKS, track.tempo());
        assertEquals(40, track.subsectionLength());

        List<Note> atTickZero = track.getNotes(0);
        assertNotNull(atTickZero);
        assertEquals(1, atTickZero.size());
        assertEquals(39, atTickZero.get(0).getPitch());   // C4 = MIDI 60 -> tone 39
        assertEquals(100, atTickZero.get(0).getVolume()); // velocity 127 -> 100

        // --- 编译阶段：CC64 踏板折算到游戏刻 ---
        // tick 0 踩下 -> 游戏刻 0；tick 960（=1 秒）抬起 -> 游戏刻 20
        assertEquals(Boolean.TRUE, track.getPedalChange(0));
        assertEquals(Boolean.FALSE, track.getPedalChange(20));
        assertEquals(2, track.pedalChanges().size());
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

    /** 一份文件如果只记录了「踏板抬起」而从没踩下过，就没有可用的释放点，应回退到按小节线释放 */
    @Test
    public void pedalWithoutAnyPressFallsBackToBarLines() {
        MidiSong song = new MidiSong("no-press.mid");
        song.ticksPerQuarter = TPQN;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, MidiTiming.DEFAULT_MICROS_PER_QUARTER));
        song.notes.add(new MidiSong.RawNote(0L, 60, 100, 0));
        song.contributingChannels.add(0);
        song.pedals.add(new MidiSong.RawPedal(0L, false, 0));

        assertTrue(song.compile().pedalChanges().isEmpty());
    }

    /** 多通道踏板取 OR：只要还有通道踩着，就不能在别的通道抬脚时提前释放 */
    @Test
    public void pedalMergesChannelsWithOr() {
        MidiSong song = new MidiSong("multi-channel.mid");
        song.ticksPerQuarter = TPQN;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, MidiTiming.DEFAULT_MICROS_PER_QUARTER));
        song.notes.add(new MidiSong.RawNote(0L, 60, 100, 0));
        song.contributingChannels.add(0);
        song.contributingChannels.add(1);

        song.pedals.add(new MidiSong.RawPedal(0L, true, 0));
        song.pedals.add(new MidiSong.RawPedal(0L, true, 1));
        song.pedals.add(new MidiSong.RawPedal(480L, false, 0));   // 通道 0 抬脚，但通道 1 还踩着
        song.pedals.add(new MidiSong.RawPedal(960L, false, 1));   // 通道 1 也抬脚，此时才真正释放

        PianoSongTrack track = song.compile();
        assertEquals(Boolean.TRUE, track.getPedalChange(0));
        assertEquals(null, track.getPedalChange(10));   // 通道 0 单独抬脚不应产生释放点
        assertEquals(Boolean.FALSE, track.getPedalChange(20));
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

        assertTrue(song.compile().pedalChanges().isEmpty());
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
