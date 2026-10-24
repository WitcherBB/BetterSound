package com.witcherbb.bettersound.music.data;

import java.util.List;
import java.util.Map;

/**
 * 服务端播放一首曲子所需的最小数据（NBS 与 MIDI 共用）。
 *
 * <p>事件表里除了正常音符，还可能夹着"释放延音"的哨兵事件（见 {@link Note#KIND_RELEASE} 等）。
 * 引擎在派发时会先把它们分流出去，所以这里是一条「同一时刻发生的所有事情」的时间轴。
 *
 * @param name             曲目名（NBS 是文件名，MIDI 是曲名），用于判断本次播放是不是同一首歌
 * @param noteMap          事件表，键是「歌曲刻」下标（读取时不做任何时间缩放，
 *                         缩放由 {@link com.witcherbb.bettersound.music.MusicTiming} 在播放时精确进行）。
 *                         MIDI 编译时把 tempo 写成「每秒 20 歌曲刻」，此时「歌曲刻」就等于游戏刻。
 * @param tempo            NBS 原始 tempo：每秒歌曲刻数 × 100（1000 即每秒 10 刻）；
 *                         MIDI 固定 2000（每秒 20 刻，即 1 歌曲刻 = 1 游戏刻）
 * @param subsectionLength 多少歌曲刻算一个小节
 * @param releaseOnBarLine 是否在每根小节线上整片释放一次延音。
 *                         NBS 恒为 true（它没有别的释放依据）；
 *                         MIDI 恒为 false——每个音都有自己的释放事件，
 *                         小节线再切一刀只会把跨小节的延音误砍掉。
 */
public record PianoSongTrack(String name, Map<Integer, List<Note>> noteMap, short tempo, int subsectionLength,
                             boolean releaseOnBarLine) {

    public PianoSongTrack {
        // tempo 与 subsectionLength 会经客户端数据包传过来，这里做防御性钳制，避免除零
        tempo = (short) Math.max(1, tempo);
        subsectionLength = Math.max(1, subsectionLength);
    }

    /** NBS 用：没有释放事件，延音释放点走小节线 */
    public PianoSongTrack(String name, Map<Integer, List<Note>> noteMap, short tempo, int subsectionLength) {
        this(name, noteMap, tempo, subsectionLength, true);
    }

    /** 该刻的事件表；{@code null} 表示这一刻什么都没有发生 */
    public List<Note> getNotes(int tick) {
        return this.noteMap.get(tick);
    }

    public int length() {
        return this.noteMap.size();
    }
}
