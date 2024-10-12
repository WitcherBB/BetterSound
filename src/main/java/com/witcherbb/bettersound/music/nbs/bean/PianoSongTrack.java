package com.witcherbb.bettersound.music.nbs.bean;

import java.util.List;
import java.util.Map;

public class PianoSongTrack {
    private final short speed;
    private final Map<Integer, List<Note>> noteMap;
    private final int subsectionLength;

    private int tick;

    public PianoSongTrack(Map<Integer, List<Note>> noteMap, short speed, int subsectionLength) {
        this.noteMap = noteMap;
        this.speed = speed;
        this.subsectionLength = subsectionLength;
    }

    public void tick() {
        this.tick++;
    }

    public List<Note> getNotes(int tick) {
        return this.noteMap.get(tick);
    }

    public int length() {
        return this.noteMap.size();
    }

    public short getSpeed() {
        return speed;
    }

    public int getSubsectionLength() {
        return subsectionLength;
    }
}
