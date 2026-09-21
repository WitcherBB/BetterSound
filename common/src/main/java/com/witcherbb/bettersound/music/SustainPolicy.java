package com.witcherbb.bettersound.music;

/**
 * 延音释放策略——NBS 与 MIDI 自动播放之间<b>唯一的实质差异</b>。
 *
 * <p>共用的引擎 {@link AutoMusicPlayer} 在每游戏刻派发音符之前问一次
 * {@link #shouldRelease(int)}，并在有音符的歌曲刻上按 {@link #releaseOnBarLine()} 决定
 * 是否走小节线释放。引擎本身不知道曲目是 NBS 还是 MIDI，差异全部落在实现类里：
 * <ul>
 *     <li>NBS：{@link #BAR_LINE}，释放点完全由小节线决定；</li>
 *     <li>MIDI：{@code com.witcherbb.bettersound.music.midi.PedalSustainPolicy}，
 *         跟随文件里记录的 CC64 踏板，并带一个防止糊成一团的安全阀。</li>
 * </ul>
 */
public interface SustainPolicy {
    /** NBS 用：没有额外释放点，释放完全交给小节线 */
    SustainPolicy BAR_LINE = new SustainPolicy() {
    };

    /**
     * 每个游戏刻询问一次：本刻是否要立刻释放延音。
     * <p>返回 true 时引擎会调用 {@code stopNote()}，把正在响的音收掉。
     * 注意这个判断挂在<b>游戏刻</b>上而不是只挂在有音符的歌曲刻上：踩/抬踏板经常落在
     * 没有音符的时刻，挂在歌曲刻上会漏掉。
     *
     * @param gameTick 当前游戏刻（从 0 开始，与音符表的键同一坐标系）
     */
    default boolean shouldRelease(int gameTick) {
        return false;
    }

    /** 有音符的歌曲刻上，是否按小节线释放延音 */
    default boolean releaseOnBarLine() {
        return true;
    }
}
