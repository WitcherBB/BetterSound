package com.witcherbb.bettersound.music.midi.bean;

import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.music.data.PianoSongTrack;
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
 * 所以 {@link #compile(int maxSustainTicks)} 只需要做时间与音量的换算。
 *
 * <p>每条音符还带着文件里记录的时长（{@link RawNote#durationTicks()}，也就是琴键按住多久）。
 * 这份数据目前只读取、未参与播放，留给后续实现「松键/断句」用。
 */
public class MidiSong {
    /**
     * 音符在文件里找不到配对的 Note Off（没有闭合）时的时长。
     * <p>不规范的 MIDI 里并不少见，遇到时保留成这个哨兵值，而不是替它猜一个数。
     */
    public static final long DURATION_UNKNOWN = -1L;

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
    /** 已通过音域与通道筛选的音符，每条都带上了文件里记录的时长（琴键按住多久） */
    public final List<RawNote> notes = new ArrayList<>();
    /** 原始的延音踏板事件 */
    public final List<RawPedal> pedals = new ArrayList<>();
    /** 真正提供了音符的通道，踏板的 OR 合并只在这些通道之间进行 */
    public final Set<Integer> contributingChannels = new HashSet<>();

    public MidiSong(String fileName) {
        this.fileName = fileName;
        this.name = fileName;
    }

    /**
     * 一个待编译的原始音符。
     *
     * @param tick          Note On 的绝对 MIDI tick
     * @param midiNote      MIDI 音高（0~127）
     * @param velocity      力度（1~127）
     * @param channel       MIDI 通道（0~15）
     * @param durationTicks 琴键按住的时长，单位是 MIDI tick（本音符 Note On 到 Note Off 的距离）；
     *                      {@link MidiSong#DURATION_UNKNOWN} 表示文件里没有配对的 Note Off。
     *                      <p>若要换算成绝对时刻，直接「结束 tick = {@code tick + durationTicks}」，
     *                      再把它交给 {@link MidiSong#toMicros(long)} 走一次速度表即可——变速曲子里也是准的。
     *                      <p>目前只读取、暂未参与播放：释放点仍然只由延音踏板（或小节线）决定。
     */
    public record RawNote(long tick, int midiNote, int velocity, int channel, long durationTicks) {

        /** 时长未知（文件里没有 Note Off）时的便捷构造 */
        public RawNote(long tick, int midiNote, int velocity, int channel) {
            this(tick, midiNote, velocity, channel, DURATION_UNKNOWN);
        }

        /** 文件里是否有配对的 Note Off */
        public boolean hasDuration() {
            return this.durationTicks >= 0;
        }

        /** 复制一份并写入时长 */
        public RawNote withDuration(long durationTicks) {
            return new RawNote(this.tick, this.midiNote, this.velocity, this.channel, durationTicks);
        }
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
     * 于是事件表的键就是游戏刻，播放引擎可以直接使用。
     * <p>
     * 「琴键什么时候松开」在这里就算完，作为哨兵事件放进事件表（{@link Note#KIND_RELEASE}）：
     * <pre>
     *     结束时刻 = 文件里写了时值 ? 音符自己的结束刻 : 本小节的末尾（没有 Note Off，只能这么假设）
     *     释放时刻 = 结束时刻踏板还踩着 ? 顺延到之后第一次抬踏板 : 结束时刻
     * </pre>
     * <b>顺延这条是关键</b>：踏板踩着的时候抬踏板，不应该影响那些"键还按着"的音——它们要一直响到
     * 自己的时值走完。于是服务端不需要记录任何"此刻还按着哪些键"的状态，一切都在这张事件表里。
     *
     * @param maxSustainTicks 踏板安全阀：连续踩着踏板超过这么多游戏刻就整片释放一次；0 表示不限制
     */
    public PianoSongTrack compile(int maxSustainTicks) {
        Map<Integer, List<Note>> eventMap = new HashMap<>();
        // 先把每个音的开始刻 / 音高 / 键松开刻记下来：释放刻要等踏板时间轴展开之后才能算
        List<NoteSpan> spans = new ArrayList<>(this.notes.size());
        for (RawNote raw : this.notes) {
            int startTick = MidiTiming.microsToGameTick(this.toMicros(raw.tick()));
            byte tone = (byte) MidiTiming.midiNoteToTone(raw.midiNote());
            byte volume = MidiTiming.velocityToVolume(raw.velocity());
            eventMap.computeIfAbsent(startTick, k -> new ArrayList<>())
                    .add(new Note(tone, volume).withDuration(this.durationInGameTicks(raw, startTick)));
            spans.add(new NoteSpan(tone, this.keyReleaseTick(raw, startTick)));
        }

        Map<Integer, Boolean> pedalChanges = this.compilePedal();
        // 曲子要照顾到的最后时刻：算一次就够，安全阀也扫同一段
        int lastTick = lastTickOf(eventMap, pedalChanges, spans);
        PedalTimeline pedal = new PedalTimeline(pedalChanges, lastTick);

        for (NoteSpan span : spans) {
            int releaseTick = pedal.releaseTickFor(span.keyReleaseTick());
            // -1：那之后踏板再也没抬起过，这个音就让它响到曲终（安全阀会兜底）
            if (releaseTick < 0) continue;
            // 插到该刻最前面：同一刻既要松键又要起新音时，必须先收旧的、再起新的。
            // 引擎目前是靠"把演奏事件攒起来、等整刻走完再统一播"保证这件事的，这里把顺序也写进数据本身。
            eventMap.computeIfAbsent(releaseTick, k -> new ArrayList<>()).add(0, Note.release(span.tone()));
        }

        // 最后把踏板状态本身也发出去：曲子里踩/抬踏板时方块状态要跟着变，模型才有反应。
        // 它只改外观、不收任何音（收不收由上面的逐音释放决定）。
        // 放在逐音释放之后发出，是为了让同一刻的先后稳定成「踏板 -> 释放 -> 演奏」。
        for (Map.Entry<Integer, Boolean> change : pedalChanges.entrySet()) {
            eventMap.computeIfAbsent(change.getKey(), k -> new ArrayList<>())
                    .add(0, change.getValue() ? Note.pedalDown() : Note.pedalUp());
        }

        if (!pedalChanges.isEmpty() && maxSustainTicks > 0) {
            appendSafetyValve(eventMap, pedal, maxSustainTicks, lastTick);
        }

        // MIDI 的释放点全部由上面的逐音事件给出，引擎不用再按小节线兜底
        return new PianoSongTrack(this.name, eventMap, (short) MidiTiming.TEMPO_AS_GAME_TICKS,
                this.barGameTicks, false);
    }

    /**
     * 琴键自己什么时候松开。
     * <p>
     * 有配对的 Note Off 就按时值算；没有的话只能假设它一直被按着，退到<b>本小节末尾</b>。
     * 这一步之后还会再过一遍踏板：那时踏板若踩着，释放点要顺延到抬踏板。
     */
    private int keyReleaseTick(RawNote raw, int startTick) {
        if (raw.hasDuration()) {
            int endTick = MidiTiming.microsToGameTick(this.toMicros(raw.tick() + raw.durationTicks()));
            return Math.max(startTick + 1, endTick);
        }
        int barLength = Math.max(1, this.barGameTicks);
        return (startTick / barLength + 1) * barLength;
    }

    /** 事件表要一直照顾到"最后一个音符刻""最后一次踏板跳变""最后一个键松开刻"里最晚的那个 */
    private static int lastTickOf(Map<Integer, List<Note>> eventMap, Map<Integer, Boolean> pedalChanges,
                                  List<NoteSpan> spans) {
        int lastTick = 0;
        for (Integer tick : eventMap.keySet()) {
            if (tick > lastTick) lastTick = tick;
        }
        for (Integer tick : pedalChanges.keySet()) {
            if (tick > lastTick) lastTick = tick;
        }
        for (NoteSpan span : spans) {
            if (span.keyReleaseTick() > lastTick) lastTick = span.keyReleaseTick();
        }
        return lastTick;
    }

    /**
     * 安全阀：踏板连续踩着超过上限时，整片释放一次。
     * <p>
     * 逐音释放已经能处理绝大多数情况，这条纯粹是兜底——踏板一直不放（或者之后再也没抬过）时，
     * 那些音的释放点会被无限顺延，总得有人来收一下。它是一把钝刀：会连"键还按着"的音一起收掉。
     * <p>
     * 计时只在踏板真的踩着时累加；踏板抬起期间不计数，重新踩下时从那一刻重新开始。
     */
    private static void appendSafetyValve(Map<Integer, List<Note>> eventMap, PedalTimeline pedal,
                                          int maxSustainTicks, int lastTick) {
        int lastRelease = 0;
        for (int tick = 0; tick <= lastTick; tick++) {
            if (!pedal.isDown(tick)) {
                lastRelease = tick;
                continue;
            }
            if (tick - lastRelease > maxSustainTicks) {
                lastRelease = tick;
                eventMap.computeIfAbsent(tick, k -> new ArrayList<>()).add(0, Note.releaseAll());
            }
        }
    }

    /** 一个音的音高，以及"琴键自己什么时候松开"（还没过踏板那一关） */
    private record NoteSpan(byte tone, int keyReleaseTick) {
    }

    /**
     * 踏板时间轴的展开形式：每一刻踏板是不是踩着，以及从某一刻往后"第一次抬踏板"是哪一刻。
     * <p>展开一次给所有音符查，避免每个音符都去扫一遍跳变表。
     */
    private static final class PedalTimeline {
        /**
         * 曲子开头踏板是不是踩着。
         * <p>{@link MidiSong#compilePedal()} 只记录真正的状态跳变，而它的起点是"没踩"，
         * 所以时间轴的第一条一定是"踩下"——在那之前踏板是抬着的。
         * 整首没有 CC64 的文件（时间轴为空）也走这条：踏板从没踩过，音符就按自己的时值松开。
         */
        private static final boolean INITIAL_DOWN = false;

        private final boolean[] downAt;
        private final int[] nextUpAt;

        PedalTimeline(Map<Integer, Boolean> changes, int lastTick) {
            int size = Math.max(1, lastTick + 1);
            this.downAt = new boolean[size];
            this.nextUpAt = new int[size];

            boolean down = INITIAL_DOWN;
            for (int tick = 0; tick < size; tick++) {
                Boolean change = changes.get(tick);
                if (change != null) down = change;
                this.downAt[tick] = down;
            }

            int nextUp = -1;
            for (int tick = size - 1; tick >= 0; tick--) {
                this.nextUpAt[tick] = nextUp;
                if (tick > 0 && this.downAt[tick - 1] && !this.downAt[tick]) nextUp = tick;
            }
        }

        boolean isDown(int tick) {
            if (tick < 0) return INITIAL_DOWN;
            return this.downAt[Math.min(tick, this.downAt.length - 1)];
        }

        /**
         * 这个音该在哪一刻释放。
         *
         * @param keyReleaseTick 琴键自己松开的时刻
         * @return 释放刻；{@code -1} 表示那之后踏板再也没抬起过，于是不主动释放
         */
        int releaseTickFor(int keyReleaseTick) {
            if (!this.isDown(keyReleaseTick)) return keyReleaseTick;
            return keyReleaseTick < this.nextUpAt.length ? this.nextUpAt[keyReleaseTick] : -1;
        }
    }

    /**
     * 把「琴键按住了多少个 MIDI tick」换算成游戏刻。
     * <p>
     * 开始与结束各自按绝对时刻取整再相减，而不是拿时长单独乘一次速度——跨过速度变化时也是准的。
     *
     * @return 游戏刻数；文件里没有配对的 Note Off 时返回 {@link Note#DURATION_UNKNOWN}
     */
    private short durationInGameTicks(RawNote raw, int startGameTick) {
        if (!raw.hasDuration()) return Note.DURATION_UNKNOWN;
        int endGameTick = MidiTiming.microsToGameTick(this.toMicros(raw.tick() + raw.durationTicks()));
        return (short) (endGameTick - startGameTick);
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
