package com.witcherbb.bettersound.music.midi;

/**
 * MIDI 时间轴与本 mod 播放模型之间的换算。
 *
 * <p>MIDI 的时间基准是「tick」，一秒里有多少 tick 由文件头的 division 决定：
 * <ul>
 *     <li>PPQ（ticks per quarter note，绝大多数文件）：需要沿 tempo map（FF 51 03 元事件）分段累加
 *         才能得到绝对微秒；默认速度是 500000 微秒/四分音符（120 BPM）。</li>
 *     <li>SMPTE：帧率固定，与 tempo 无关，tick 可直接换算成微秒。</li>
 * </ul>
 *
 * <p>而 Minecraft 的声音只能落在 20 刻/秒的游戏刻网格上，所以这里统一换算成「游戏刻」：
 * <pre>
 *     gameTick = round(micros * 20 / 1000000)
 * </pre>
 * 单个音符的误差不超过半个游戏刻（25ms），且是按绝对时刻换算、不会随时间累积。
 * 50ms 是游戏刻粒度下的物理极限，更细的差别本 mod 无法表现。
 *
 * <p>把结果喂给 NBS 播放引擎时，{@code PianoSongTrack.tempo} 固定写成
 * {@link #TEMPO_AS_GAME_TICKS}，此时 {@link com.witcherbb.bettersound.music.MusicTiming#toGameTick}
 * 退化成恒等映射（见该常量注释），于是音符表的键就是游戏刻，播放引擎无需任何改动。
 *
 * <p>本类是纯计算、不依赖任何 Minecraft 与 javax.sound.midi 类型，便于单测。
 */
public final class MidiTiming {
    /** Minecraft 固定 20 游戏刻/秒 */
    public static final int GAME_TICKS_PER_SECOND = 20;
    public static final long MICROS_PER_SECOND = 1_000_000L;
    /** MIDI 默认速度：500000 微秒/四分音符，即 120 BPM */
    public static final long DEFAULT_MICROS_PER_QUARTER = 500_000L;

    /**
     * 编译结果写入 {@code PianoSongTrack.tempo} 的固定值：每秒 20 个「歌曲刻」，即 1 歌曲刻 = 1 游戏刻。
     * <p>
     * {@code MusicTiming.toGameTick(n, 2000) = (2 * n * 20 * 100 + 2000) / (2 * 2000) = (4000n + 2000) / 4000 = n}，
     * 整数除法下取整后恰好恒等，所以 MIDI 的音符表可以直接以游戏刻为键交给 NBS 的播放引擎。
     */
    public static final int TEMPO_AS_GAME_TICKS = 2000;

    /** 钢琴最低键 A0 的 MIDI 音高 */
    public static final int PIANO_LOW_MIDI_NOTE = 21;
    /** 钢琴最高键 C8 的 MIDI 音高 */
    public static final int PIANO_HIGH_MIDI_NOTE = 108;
    /** 钢琴键数（88） */
    public static final int PIANO_KEY_COUNT = PIANO_HIGH_MIDI_NOTE - PIANO_LOW_MIDI_NOTE + 1;

    /** 无拍号信息时按 4/4 处理 */
    public static final int DEFAULT_TIME_SIGNATURE_NUMERATOR = 4;
    /** 4/4 的分母是 2 的 2 次方 */
    public static final int DEFAULT_TIME_SIGNATURE_DENOMINATOR_POWER = 2;
    /** SMPTE 文件没有 BPM，无法由拍号推出小节时长，退化成 2 秒（40 游戏刻）一节 */
    public static final int DEFAULT_BAR_GAME_TICKS = 2 * GAME_TICKS_PER_SECOND;

    /** MIDI 里的打击乐通道（0 基），GM 惯例的「第 10 通道」 */
    public static final int DRUM_CHANNEL = 9;

    private MidiTiming() {
    }

    /**
     * 绝对微秒换算成最接近的游戏刻序号（用整数运算四舍五入，避免浮点误差）。
     */
    public static int microsToGameTick(long micros) {
        return (int) ((micros * GAME_TICKS_PER_SECOND + MICROS_PER_SECOND / 2) / MICROS_PER_SECOND);
    }

    /** MIDI 音高是否落在钢琴 88 键之内（21=A0 ~ 108=C8） */
    public static boolean isInPianoRange(int midiNote) {
        return midiNote >= PIANO_LOW_MIDI_NOTE && midiNote <= PIANO_HIGH_MIDI_NOTE;
    }

    /** MIDI 音高换算成钢琴键号（0=A0 ~ 87=C8）；调用前请先用 {@link #isInPianoRange} 过滤 */
    public static int midiNoteToTone(int midiNote) {
        return midiNote - PIANO_LOW_MIDI_NOTE;
    }

    /**
     * MIDI 力度（0~127）换算成 mod 内的音符音量（1~100）。
     * <p>音量最终由 {@code Note.toPianoSoundVolume} 折算成 {@code volume * 3.0F / 100} 的播放音量。
     */
    public static byte velocityToVolume(int velocity) {
        int volume = Math.round(velocity * 100.0F / 127.0F);
        return (byte) Math.max(1, Math.min(100, volume));
    }

    /**
     * 一小节有多少个游戏刻。
     *
     * @param numerator          拍号分子，例如 4/4 的 4
     * @param denominatorPower   拍号分母是 2 的多少次方，例如 4/4 的 2、6/8 的 3
     * @param ticksPerQuarter    PPQ 分辨率
     * @param microsPerQuarter   该小节起始处生效的每四分音符微秒数
     */
    public static int barGameTicks(int numerator, int denominatorPower, int ticksPerQuarter, long microsPerQuarter) {
        if (numerator <= 0) numerator = DEFAULT_TIME_SIGNATURE_NUMERATOR;
        if (denominatorPower < 0 || denominatorPower > 8) denominatorPower = DEFAULT_TIME_SIGNATURE_DENOMINATOR_POWER;
        if (ticksPerQuarter <= 0) return DEFAULT_BAR_GAME_TICKS;

        // 一小节有多少个四分音符：4/4 -> 4，6/8 -> 3
        long barTicks = (long) numerator * 4L * ticksPerQuarter / (1L << denominatorPower);
        long barMicros = barTicks * Math.max(1L, microsPerQuarter) / ticksPerQuarter;
        return Math.max(1, microsToGameTick(barMicros));
    }

    /** SMPTE 模式下每秒多少个 tick：帧率 × 每帧 tick 数 */
    public static int smpteTicksPerSecond(float framesPerSecond, int ticksPerFrame) {
        return Math.max(1, Math.round(framesPerSecond * Math.max(1, ticksPerFrame)));
    }

    /** SMPTE 模式下 tick 换算成绝对微秒（与 tempo 无关） */
    public static long smpteToMicros(long tick, int ticksPerSecond) {
        return tick * MICROS_PER_SECOND / Math.max(1, ticksPerSecond);
    }

    /**
     * 一段恒定速度区间：从 MIDI {@code startTick} 起速度变为 {@code microsPerQuarter}，
     * 且该 tick 处已经累计了 {@code startMicros} 微秒。
     */
    public record TempoSegment(long startTick, long startMicros, long microsPerQuarter) {
    }

    /** 把某段速度区间内的 MIDI tick 换算成绝对微秒 */
    public static long toMicros(long tick, TempoSegment segment, int ticksPerQuarter) {
        return segment.startMicros()
                + (tick - segment.startTick()) * segment.microsPerQuarter() / Math.max(1, ticksPerQuarter);
    }
}
