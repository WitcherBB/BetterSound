package com.witcherbb.bettersound.music.nbs;

/**
 * NBS 歌曲刻与 Minecraft 游戏刻之间的换算。
 * <p>
 * NBS 文件头里的 tempo 字段单位是「每秒歌曲刻数 × 100」（1000 就是每秒 10 个歌曲刻，
 * 即 NBS 的默认速度）。而 Minecraft 的声音只能落在 20 刻/秒的游戏刻网格上，
 * 所以「一个歌曲刻占几个游戏刻」通常不是整数：例如 16 刻/秒 → 1.25 游戏刻/歌曲刻。
 * <p>
 * 本类给出绝对换算（四舍五入到最近的游戏刻）：
 * <pre>
 *     gameTick(n) = round(n * 20 * 100 / tempo)
 * </pre>
 * 两点关键：
 * <ul>
 *     <li>用整数运算，不引入浮点误差；</li>
 *     <li>是「按歌曲刻下标绝对换算」而不是「逐音符累加间隔」，所以单个音符的时刻误差
 *         不超过半个游戏刻（25ms），且永远不会随时间累积——整首歌的总时长与 NBS
 *         的偏差同样不超过半个游戏刻。</li>
 * </ul>
 * 换算结果非递减，因此多个歌曲刻落到同一游戏刻时，它们会在同一游戏刻里依次发声
 * （tempo 高于 20 刻/秒时必然如此，这是游戏刻粒度下的物理极限）。
 */
public final class NbsTiming {
    /** Minecraft 服务端固定 20 游戏刻/秒 */
    public static final int GAME_TICKS_PER_SECOND = 20;
    /** NBS tempo 字段的单位：每秒歌曲刻数 × 100 */
    public static final int TEMPO_SCALE = 100;
    /** NBS 默认速度：每秒 10 个歌曲刻 */
    public static final short DEFAULT_TEMPO = 10 * TEMPO_SCALE;

    private NbsTiming() {
    }

    /**
     * 把第 {@code songTick} 个 NBS 歌曲刻换算成最接近的游戏刻序号。
     *
     * @param songTick NBS 歌曲刻下标，从 0 开始
     * @param tempo    NBS 原始 tempo（每秒歌曲刻数 × 100），小于 1 时按 1 处理
     * @return 该歌曲刻最接近的游戏刻序号
     */
    public static int toGameTick(int songTick, int tempo) {
        int t = Math.max(1, tempo);
        // round(songTick * 20 * 100 / t)：分子分母同乘 2，避免 t 为奇数时 t / 2 被截断
        return (int) ((2L * songTick * GAME_TICKS_PER_SECOND * TEMPO_SCALE + t) / (2L * t));
    }

    /** 每个歌曲刻占用多少个游戏刻（仅用于显示与调试） */
    public static double gameTicksPerSongTick(int tempo) {
        return (double) (GAME_TICKS_PER_SECOND * TEMPO_SCALE) / Math.max(1, tempo);
    }

    /** 每秒多少个歌曲刻（仅用于显示与调试） */
    public static double songTicksPerSecond(int tempo) {
        return Math.max(1, tempo) / (double) TEMPO_SCALE;
    }
}
