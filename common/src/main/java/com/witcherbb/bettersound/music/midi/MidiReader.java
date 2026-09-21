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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

        for (Track track : sequence.getTracks()) {
            String trackName = null;
            int trackNoteCount = 0;

            for (int i = 0, size = track.size(); i < size; i++) {
                MidiEvent event = track.get(i);
                long tick = event.getTick();
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage shortMessage) {
                    int command = shortMessage.getCommand();
                    if (command == ShortMessage.NOTE_ON && shortMessage.getData2() > 0) {
                        // 力度为 0 的 Note On 等价于 Note Off，已被上面的条件排除
                        if (this.collectNote(song, tick, shortMessage.getData1(), shortMessage.getData2(), shortMessage.getChannel())) {
                            trackNoteCount++;
                        }
                    } else if (command == ShortMessage.CONTROL_CHANGE && shortMessage.getData1() == SUSTAIN_PEDAL_CONTROLLER) {
                        song.pedals.add(new MidiSong.RawPedal(tick,
                                shortMessage.getData2() >= SUSTAIN_PEDAL_DOWN_THRESHOLD, shortMessage.getChannel()));
                    }
                    // Note Off、Pitch Bend、其它控制器、Aftertouch 一律忽略：
                    // 本 mod 的模型里没有「松键」概念，音符一律自然衰减，释放点只由延音踏板决定
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
     * 收集一个音符：越界音高与打击乐通道在这里就被丢弃，只有能真正弹出来的音符才会进入 {@link MidiSong}。
     *
     * @return 是否收录
     */
    private boolean collectNote(MidiSong song, long tick, int midiNote, int velocity, int channel) {
        song.totalNotes++;
        if (channel == MidiTiming.DRUM_CHANNEL) {
            song.drumNotes++;
            return false;
        }
        if (!MidiTiming.isInPianoRange(midiNote)) {
            song.outOfRangeNotes++;
            return false;
        }
        song.notes.add(new MidiSong.RawNote(tick, midiNote, velocity, channel));
        song.contributingChannels.add(channel);
        return true;
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
