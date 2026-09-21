package com.witcherbb.bettersound.music.midi;

import com.witcherbb.bettersound.music.SustainPolicy;

import java.util.Map;

/**
 * 跟随 MIDI 里 CC64 踏板记录的延音释放策略。
 *
 * <p>规则（与 {@code MidiSong.compilePedal} 编译出来的那张稀疏跳变表配套）：
 * <ul>
 *     <li>只有「踏板抬起」的跳变有可听效果——它让引擎释放一次，把正在响的音收掉；
 *         「踏板踩下」不需要额外动作，因为引擎的 {@code playNote} 本来就强制延音，
 *         所以踏板抬起之后新弹的音依然是饱满的长音，不会变成短促的断奏。</li>
 *     <li>安全阀：曲子里踏板一直踩住不放时，超过 {@code maxSustainTicks} 就强制释放一次，
 *         避免声音叠在一起糊成一片。0 表示关闭。</li>
 *     <li>踏板跳变表为空（文件没有可用的释放点）时，{@link #releaseOnBarLine()} 返回 true，
 *         引擎自动回退到按小节线释放，也就是 NBS 的行为。</li>
 * </ul>
 */
public final class PedalSustainPolicy implements SustainPolicy {
    /** 游戏刻 -> 该刻起生效的踏板状态 */
    private final Map<Integer, Boolean> pedalChanges;
    /** 踏板最长保持多少个游戏刻；0 表示关闭安全阀 */
    private final int maxSustainTicks;
    /** 当前踏板状态；曲子开始播放时视为踩着，与「只认抬起跳变」的规则一致 */
    private boolean pedalDown = true;
    /** 上次释放延音的游戏刻，用于安全阀计时 */
    private int lastReleaseTick;

    public PedalSustainPolicy(Map<Integer, Boolean> pedalChanges, int maxSustainTicks) {
        this.pedalChanges = pedalChanges == null ? Map.of() : pedalChanges;
        this.maxSustainTicks = Math.max(0, maxSustainTicks);
    }

    @Override
    public boolean shouldRelease(int gameTick) {
        boolean release = false;

        Boolean state = this.pedalChanges.get(gameTick);
        if (state != null && state != this.pedalDown) {
            this.pedalDown = state;
            release = !state;
        }

        if (!release && this.maxSustainTicks > 0 && this.pedalDown
                && gameTick - this.lastReleaseTick > this.maxSustainTicks) {
            release = true;
        }

        if (release) {
            this.lastReleaseTick = gameTick;
        }
        return release;
    }

    @Override
    public boolean releaseOnBarLine() {
        return this.pedalChanges.isEmpty();
    }
}
