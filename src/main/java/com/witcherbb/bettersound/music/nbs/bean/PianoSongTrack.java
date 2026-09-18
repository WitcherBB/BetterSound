package com.witcherbb.bettersound.music.nbs.bean;

import java.util.List;
import java.util.Map;

/**
 * 服务端播放一首 NBS 所需的最小数据。
 *
 * @param name             歌曲文件名，用于判断本次播放是不是同一首歌
 * @param noteMap          音符表，键是「NBS 歌曲刻」下标（读取时不做任何时间缩放，
 *                         缩放由 {@link com.witcherbb.bettersound.music.nbs.NbsTiming} 在播放时精确进行）
 * @param tempo            NBS 原始 tempo：每秒歌曲刻数 × 100（1000 即每秒 10 刻）
 * @param subsectionLength 多少歌曲刻算一个小节，用于在小节线上释放延音
 */
public record PianoSongTrack(String name, Map<Integer, List<Note>> noteMap, short tempo, int subsectionLength) {

    public PianoSongTrack {
        // tempo 与 subsectionLength 会经客户端数据包传过来，这里做防御性钳制，避免除零
        tempo = (short) Math.max(1, tempo);
        subsectionLength = Math.max(1, subsectionLength);
    }

    public List<Note> getNotes(int tick) {
        return this.noteMap.get(tick);
    }

    public int length() {
        return this.noteMap.size();
    }
}
