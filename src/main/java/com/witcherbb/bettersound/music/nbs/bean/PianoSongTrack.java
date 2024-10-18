package com.witcherbb.bettersound.music.nbs.bean;

import java.util.List;
import java.util.Map;

public record PianoSongTrack(String name, Map<Integer, List<Note>> noteMap, short speed, int subsectionLength) {

    public List<Note> getNotes(int tick) {
        return this.noteMap.get(tick);
    }

    public int length() {
        return this.noteMap.size();
    }
}
