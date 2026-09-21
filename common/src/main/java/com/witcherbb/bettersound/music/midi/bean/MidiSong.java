package com.witcherbb.bettersound.music.midi.bean;

import com.witcherbb.bettersound.music.bean.Note;
import com.witcherbb.bettersound.music.bean.PianoSongTrack;
import com.witcherbb.bettersound.music.midi.MidiTiming;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 一个 MIDI 文件的解析结果（客户端）。
 *
 * <p>字段风格与 NBS 的 {@code PianoSong} 保持一致：由 {@link com.witcherbb.bettersound.music.midi.MidiReader}
 * 直接写入公开字段，不做过度的封装。
 *
 * <p>注意 {@link #notes} 里只保留「能被钢琴弹出来」的音符：打击乐通道与越界音高在解析阶段就被剔除，
 * 所以 {@link #compile()} 只需要做时间与音量的换算。
 */
public class MidiSong {
    /** 文件名（含扩展名），用于网络传输后的同名判定 */
    public final String fileName;
    /** 曲名：优先取 MIDI 里的轨道名，没有就用文件名 */
    public String name;

    /** PPQ 分辨率；SMPTE 文件为 0 */
    public int ticksPerQuarter;
    /** SMPTE 每秒 tick 数；PPQ 文件为 0 */
    public int smpteTicksPerSecond;

    public int timeSignatureNumerator = MidiTiming.DEFAULT_TIME_SIGNATURE_NUMERATOR;
    public int timeSignatureDenominatorPower = MidiTiming.DEFAULT_TIME_SIGNATURE_DENOMINATOR_POWER;
    /** 一小节折算成多少游戏刻，用于踏板安全阀计时 */
    public int barGameTicks = MidiTiming.DEFAULT_BAR_GAME_TICKS;

    /** 文件里的音符总数（含被丢弃的） */
    public int totalNotes;
    /** 因为落在钢琴音域之外被丢弃的音符数 */
    public int outOfRangeNotes;
    /** 因为属于打击乐通道被丢弃的音符数 */
    public int drumNotes;

    /** 速度区间表，至少含一段（tick 0 处的默认速度） */
    public final List<MidiTiming.TempoSegment> tempoSegments = new ArrayList<>();
    /** 已通过音域与通道筛选的音符 */
    public final List<RawNote> notes = new ArrayList<>();
    /** 原始的延音踏板事件 */
    public final List<RawPedal> pedals = new ArrayList<>();
    /** 真正提供了音符的通道，踏板的 OR 合并只在这些通道之间进行 */
    public final Set<Integer> contributingChannels = new HashSet<>();

    public MidiSong(String fileName) {
        this.fileName = fileName;
        this.name = fileName;
    }

    /** 一个待编译的原始音符 */
    public record RawNote(long tick, int midiNote, int velocity, int channel) {
    }

    /** 一次延音踏板状态变化（CC64） */
    public record RawPedal(long tick, boolean down, int channel) {
    }

    /** MIDI tick 换算成绝对微秒 */
    public long toMicros(long tick) {
        if (this.smpteTicksPerSecond > 0) {
            return MidiTiming.smpteToMicros(tick, this.smpteTicksPerSecond);
        }
        return MidiTiming.toMicros(tick, this.segmentAt(tick), Math.max(1, this.ticksPerQuarter));
    }

    /** 由拍号与起始速度推出小节长度，需要信息齐全后调用 */
    public int computeBarGameTicks() {
        if (this.smpteTicksPerSecond > 0) {
            // SMPTE 文件里没有 BPM，拍号只是记谱信息，推不出真实小节时长
            return MidiTiming.DEFAULT_BAR_GAME_TICKS;
        }
        return MidiTiming.barGameTicks(this.timeSignatureNumerator, this.timeSignatureDenominatorPower,
                Math.max(1, this.ticksPerQuarter), this.segmentAt(0L).microsPerQuarter());
    }

    /**
     * 编译成服务端播放所需的 {@link PianoSongTrack}。
     * <p>
     * 时间轴的处理见 {@link MidiTiming}：tempo 固定写成 {@code TEMPO_AS_GAME_TICKS}，
     * 于是音符表的键就是游戏刻，NBS 的播放引擎可以直接使用。
     */
    public PianoSongTrack compile() {
        Map<Integer, List<Note>> noteMap = new HashMap<>();
        for (RawNote raw : this.notes) {
            int gameTick = MidiTiming.microsToGameTick(this.toMicros(raw.tick()));
            byte tone = (byte) MidiTiming.midiNoteToTone(raw.midiNote());
            byte volume = MidiTiming.velocityToVolume(raw.velocity());
            noteMap.computeIfAbsent(gameTick, k -> new ArrayList<>()).add(new Note(tone, volume));
        }
        return new PianoSongTrack(this.name, noteMap, (short) MidiTiming.TEMPO_AS_GAME_TICKS,
                this.barGameTicks, this.compilePedal());
    }

    /**
     * 把 CC64 事件编译成「游戏刻 -> 踏板状态」的稀疏跳变表。
     * <p>
     * 规则：
     * <ul>
     *     <li>只统计真正提供了音符的通道，多通道之间取 OR（任一通道踩下就算踩下），
     *         避免伴奏轨的踏板影响钢琴的释放点；</li>
     *     <li>同一 tick 上的多条事件先全部应用再判定状态，同游戏刻的多次跳变以后者为准；</li>
     *     <li>返回空表表示「这份文件没有可用的释放点」，此时播放引擎会回退到按小节线释放——
     *         这也包括「整首曲子踏板一直没踩下」的情况，否则所有音会一直响下去糊成一片。</li>
     * </ul>
     */
    private Map<Integer, Boolean> compilePedal() {
        if (this.pedals.isEmpty() || this.contributingChannels.isEmpty()) return Map.of();

        List<RawPedal> sorted = new ArrayList<>(this.pedals);
        sorted.sort(Comparator.comparingLong(RawPedal::tick));

        Map<Integer, Boolean> channelState = new HashMap<>();
        Map<Integer, Boolean> changes = new LinkedHashMap<>();
        boolean pedalDown = false;

        int index = 0;
        int size = sorted.size();
        while (index < size) {
            long tick = sorted.get(index).tick();
            while (index < size && sorted.get(index).tick() == tick) {
                RawPedal pedal = sorted.get(index++);
                if (this.contributingChannels.contains(pedal.channel())) {
                    channelState.put(pedal.channel(), pedal.down());
                }
            }
            boolean down = channelState.containsValue(Boolean.TRUE);
            if (down != pedalDown) {
                pedalDown = down;
                changes.put(MidiTiming.microsToGameTick(this.toMicros(tick)), down);
            }
        }
        return changes;
    }

    /** 找到 tick 所在的速度区间：取 startTick 不大于 tick 的最后一段 */
    private MidiTiming.TempoSegment segmentAt(long tick) {
        List<MidiTiming.TempoSegment> segments = this.tempoSegments;
        int low = 0;
        int high = segments.size() - 1;
        int found = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (segments.get(mid).startTick() <= tick) {
                found = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return segments.get(found);
    }
}
