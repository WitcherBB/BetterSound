package com.witcherbb.bettersound.music.midi;

import com.witcherbb.bettersound.exception.FileIsNotMidiException;
import com.witcherbb.bettersound.music.midi.bean.MidiSong;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MIDI 文件读取器（客户端）。
 *
 * <p>本类是整条 MIDI 链路里唯一接触 {@code javax.sound.midi} 的地方，而且是纯客户端类
 * （原来是 Forge 的 {@code @OnlyIn(Dist.CLIENT)}，迁移到 common 时已去掉该注解与 import）：
 * 因此服务端不需要 {@code java.desktop}——解析永远发生在客户端，服务端只会收到编译好的裸数据。
 *
 * <p>只用 {@link MidiSystem#getSequence(File)} 做纯粹的文本解析，<b>绝不能</b>使用
 * {@code getSequencer()} / {@code getSoundbank()} / {@code getReceiver()}——那些会去初始化音频设备，
 * 在无头或无声卡的环境下会抛 {@code MidiUnavailableException}。
 */
public class MidiReader implements Closeable {
    /** 支持的扩展名；{@code .rmi} 是 RIFF 包装的 MIDI，JDK 也能直接解析 */
    public static final List<String> MIDI_EXTENSIONS = List.of(".mid", ".midi", ".rmi");

    /** CC64：延音踏板控制器编号 */
    private static final int SUSTAIN_PEDAL_CONTROLLER = 64;
    /** CC64 的值 >= 64 视为踏板踩下 */
    private static final int SUSTAIN_PEDAL_DOWN_THRESHOLD = 64;

    private static final int META_TEMPO = 0x51;
    private static final int META_TIME_SIGNATURE = 0x58;
    private static final int META_TRACK_NAME = 0x03;

    private final File file;
    private final String fileName;

    public MidiReader(String path) throws FileIsNotMidiException {
        this(new File(path));
    }

    public MidiReader(File file) throws FileIsNotMidiException {
        if (!hasMidiExtension(file.getName())) throw new FileIsNotMidiException(file);
        this.file = file;
        this.fileName = file.getName();
    }

    public MidiSong readSong() throws IOException, InvalidMidiDataException {
        Sequence sequence = MidiSystem.getSequence(this.file);

        MidiSong song = new MidiSong(this.fileName);
        if (sequence.getDivisionType() == Sequence.PPQ) {
            song.ticksPerQuarter = Math.max(1, sequence.getResolution());
        } else {
            // SMPTE：帧率由 division 决定，与 tempo 无关
            song.smpteTicksPerSecond = MidiTiming.smpteTicksPerSecond(sequence.getDivisionType(), sequence.getResolution());
        }

        List<long[]> tempoEvents = new ArrayList<>();
        boolean hasTimeSignature = false;
        String bestTrackName = null;
        int bestTrackNoteCount = 0;

        // 等着 Note Off 的 Note On：键是「通道 + 音高」，值是这些音符在 song.notes 里的下标队列。
        // 同一个 (通道, 音高) 可能重叠着响好几个，所以用队列按先后顺序配对。
        Map<Integer, Deque<Integer>> pendingNotes = new HashMap<>();

        for (Track track : sequence.getTracks()) {
            String trackName = null;
            int trackNoteCount = 0;

            for (int i = 0, size = track.size(); i < size; i++) {
                MidiEvent event = track.get(i);
                long tick = event.getTick();
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage shortMessage) {
                    int command = shortMessage.getCommand();
                    int data1 = shortMessage.getData1();
                    int data2 = shortMessage.getData2();
                    int channel = shortMessage.getChannel();
                    if (command == ShortMessage.NOTE_ON && data2 > 0) {
                        int index = this.collectNote(song, tick, data1, data2, channel);
                        if (index >= 0) {
                            trackNoteCount++;
                            pendingNotes.computeIfAbsent(noteKey(channel, data1), key -> new ArrayDeque<>()).add(index);
                        }
                    } else if (command == ShortMessage.NOTE_OFF || (command == ShortMessage.NOTE_ON && data2 == 0)) {
                        // 力度为 0 的 Note On 与 Note Off 表达的是同一件事，这里一并处理：
                        // 它给出这个音符的结束时刻，两相减就是琴键按住的时长
                        this.closeNote(song, pendingNotes, tick, channel, data1);
                    } else if (command == ShortMessage.CONTROL_CHANGE && data1 == SUSTAIN_PEDAL_CONTROLLER) {
                        song.pedals.add(new MidiSong.RawPedal(tick, data2 >= SUSTAIN_PEDAL_DOWN_THRESHOLD, channel));
                    }
                    // Pitch Bend、其它控制器、Aftertouch 仍然忽略
                } else if (message instanceof MetaMessage metaMessage) {
                    byte[] data = metaMessage.getData();
                    switch (metaMessage.getType()) {
                        case META_TEMPO -> {
                            if (data.length >= 3) {
                                long microsPerQuarter = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                                if (microsPerQuarter > 0) tempoEvents.add(new long[]{tick, microsPerQuarter});
                            }
                        }
                        case META_TIME_SIGNATURE -> {
                            // 只取第一处拍号：小节长度按开头的拍号算
                            if (data.length >= 2 && !hasTimeSignature) {
                                song.timeSignatureNumerator = data[0] & 0xFF;
                                song.timeSignatureDenominatorPower = data[1] & 0xFF;
                                hasTimeSignature = true;
                            }
                        }
                        case META_TRACK_NAME -> {
                            if (trackName == null) {
                                String value = new String(data, StandardCharsets.UTF_8).trim();
                                if (!value.isEmpty()) trackName = value;
                            }
                        }
                        default -> {
                        }
                    }
                }
            }

            // 曲名取「音符最多的那条轨」的名字：Format 1 的第 0 轨常常只放速度信息、
            // 名字是 "Tempo"/"Conductor" 之类，直接取第一条会显示成怪名字。
            if (trackName != null && trackNoteCount > bestTrackNoteCount) {
                bestTrackName = trackName;
                bestTrackNoteCount = trackNoteCount;
            }
        }

        this.buildTempoMap(song, tempoEvents);
        if (bestTrackName != null) song.name = bestTrackName;
        song.barGameTicks = song.computeBarGameTicks();
        return song;
    }

    /**
     * 收集一个 Note On：越界音高与打击乐通道在这里就被丢弃，只有能真正弹出来的音符才会进入 {@link MidiSong}。
     * <p>音高是否收录只取决于「通道 + 音高」，所以同一个键上的 Note On / Note Off 要么全部收录、要么全部丢弃，
     * 配对时不会错位。
     *
     * @return 该音符在 {@code song.notes} 里的下标；被丢弃时返回 -1
     */
    private int collectNote(MidiSong song, long tick, int midiNote, int velocity, int channel) {
        song.totalNotes++;
        if (channel == MidiTiming.DRUM_CHANNEL) {
            song.drumNotes++;
            return -1;
        }
        if (!MidiTiming.isInPianoRange(midiNote)) {
            song.outOfRangeNotes++;
            return -1;
        }
        song.notes.add(new MidiSong.RawNote(tick, midiNote, velocity, channel));
        song.contributingChannels.add(channel);
        return song.notes.size() - 1;
    }

    /** 把「通道 + 音高」合成一个键：只有同通道同音高才需要互相配对 */
    private static int noteKey(int channel, int midiNote) {
        return (channel << 8) | (midiNote & 0xFF);
    }

    /**
     * 处理一个 Note Off：把它配对回同通道同音高、最早那个还没闭合的 Note On，
     * 算出的时长（琴键按住多久）写回那条音符记录。
     * <p>
     * 找不到对应 Note On 的 Note Off（文件不规范）直接忽略；反过来一直没等到 Note Off 的音符，
     * 时长会保持 {@link MidiSong#DURATION_UNKNOWN}。
     */
    private void closeNote(MidiSong song, Map<Integer, Deque<Integer>> pendingNotes, long tick, int channel, int midiNote) {
        Deque<Integer> waiting = pendingNotes.get(noteKey(channel, midiNote));
        if (waiting == null || waiting.isEmpty()) return;

        int index = waiting.pollFirst();
        MidiSong.RawNote note = song.notes.get(index);
        if (tick < note.tick()) return; // 畸形文件：结束早于开始，宁可留成未知
        song.notes.set(index, note.withDuration(tick - note.tick()));
    }

    /**
     * 把散落在各轨的 tempo 元事件整理成升序的速度区间表，每段记录「起始 tick / 该 tick 处的累计微秒 / 每四分音符微秒数」。
     */
    private void buildTempoMap(MidiSong song, List<long[]> tempoEvents) {
        long currentMicros = MidiTiming.DEFAULT_MICROS_PER_QUARTER;
        song.tempoSegments.add(new MidiTiming.TempoSegment(0L, 0L, currentMicros));
        if (song.ticksPerQuarter <= 0) return; // SMPTE 文件的时间与 tempo 无关

        tempoEvents.sort(Comparator.comparingLong(event -> event[0]));
        long elapsedMicros = 0L;
        long lastTick = 0L;
        for (long[] event : tempoEvents) {
            long tick = event[0];
            long microsPerQuarter = event[1];
            elapsedMicros += (tick - lastTick) * currentMicros / song.ticksPerQuarter;
            lastTick = tick;
            if (microsPerQuarter == currentMicros) continue;

            currentMicros = microsPerQuarter;
            if (tick == 0L && song.tempoSegments.size() == 1) {
                // 开头就改速度：直接替换掉默认段，避免留下两个 startTick=0 的区间
                song.tempoSegments.set(0, new MidiTiming.TempoSegment(0L, 0L, currentMicros));
            } else {
                song.tempoSegments.add(new MidiTiming.TempoSegment(tick, elapsedMicros, currentMicros));
            }
        }
    }

    public static boolean hasMidiExtension(String fileName) {
        String lowerCase = fileName.toLowerCase(Locale.ROOT);
        for (String extension : MIDI_EXTENSIONS) {
            if (lowerCase.endsWith(extension)) return true;
        }
        return false;
    }

    @Override
    public void close() {
        // MidiSystem.getSequence 会一次性把文件读完，没有需要释放的句柄
    }
}
