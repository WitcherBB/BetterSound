package com.witcherbb.bettersound.music.bean;

import java.util.List;
import java.util.Map;

/**
 * 服务端播放一首曲子所需的最小数据（NBS 与 MIDI 共用）。
 *
 * @param name             曲目名（NBS 是文件名，MIDI 是曲名），用于判断本次播放是不是同一首歌
 * @param noteMap          音符表，键是「歌曲刻」下标（读取时不做任何时间缩放，
 *                         缩放由 {@link com.witcherbb.bettersound.music.MusicTiming} 在播放时精确进行）。
 *                         MIDI 编译时把 tempo 写成「每秒 20 歌曲刻」，此时「歌曲刻」就等于游戏刻。
 * @param tempo            NBS 原始 tempo：每秒歌曲刻数 × 100（1000 即每秒 10 刻）；
 *                         MIDI 固定 2000（每秒 20 刻，即 1 歌曲刻 = 1 游戏刻）
 * @param subsectionLength 多少歌曲刻算一个小节：NBS 用于在小节线上释放延音；
 *                         MIDI 只作为「踏板安全阀」的计时基准（见 pedalChanges）
 * @param pedalChanges     延音踏板时间轴：键是游戏刻，值是该刻起生效的踏板状态。
 *                         空表表示没有踏板信息，延音释放点回退到小节线——NBS 恒为空表，
 *                         因此 NBS 的播放行为与加入本字段之前完全一致。
 *                         MIDI 的这份数据由 CC64 控制器事件编译而来。
 */
public record PianoSongTrack(String name, Map<Integer, List<Note>> noteMap, short tempo, int subsectionLength,
                             Map<Integer, Boolean> pedalChanges) {

    public PianoSongTrack {
        // tempo 与 subsectionLength 会经客户端数据包传过来，这里做防御性钳制，避免除零
        tempo = (short) Math.max(1, tempo);
        subsectionLength = Math.max(1, subsectionLength);
        pedalChanges = pedalChanges == null ? Map.of() : pedalChanges;
    }

    /** NBS 用：没有踏板时间轴，延音释放点走小节线 */
    public PianoSongTrack(String name, Map<Integer, List<Note>> noteMap, short tempo, int subsectionLength) {
        this(name, noteMap, tempo, subsectionLength, Map.of());
    }

    public List<Note> getNotes(int tick) {
        return this.noteMap.get(tick);
    }

    /** 该游戏刻是否有踏板状态变化；{@code null} 表示没有 */
    public Boolean getPedalChange(int gameTick) {
        return this.pedalChanges.get(gameTick);
    }

    public int length() {
        return this.noteMap.size();
    }
}
