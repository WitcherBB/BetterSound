package com.witcherbb.bettersound.music.nbs.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PianoSong {
    public String fileName;
    public byte nbsVersion;
    public byte instrumentCount;
    public short length;
    public short layerCount;
    public String name;
    public String author;
    public String originalAuthor;
    public String description;
    public short speed;
    public byte autoSaving;
    public byte autoSavingDuration;
    public byte timeSignature;
    public int minutesSpent;
    public int leftClicks;
    public int rightClicks;
    public int noteBlocksAdded;
    public int noteBlocksRemoved;
    public String midiOrSchematicFileName;
    public byte loop;
    public byte maxLoopCount;
    public short loopStartTick;
    private boolean newNBS;

    private final Map<Integer, List<Note>> noteMap;
    private final List<Byte> layerVolumes;

    public PianoSong(String fileName) {
        this.noteMap = new HashMap<>();
        this.layerVolumes = new ArrayList<>();
        this.fileName = fileName;
    }

    public void addNote(int tick, byte tone, byte volume, short layer) {
        int jumps = tick * this.speed;
        if (this.noteMap.containsKey(jumps)) {
            this.noteMap.get(jumps).add(new Note(tone, volume).withLayer((byte) layer));
        } else {
            List<Note> noteList = new ArrayList<>();
            noteList.add(new Note(tone, volume).withLayer((byte) layer));
            this.noteMap.put(jumps, noteList);
        }
    }

    public void parse() {
        for (List<Note> notes : noteMap.values()) {
            int size = notes.size();
            for (int i = 0; i < size; i++) {
                Note note = notes.get(i);
                byte volume = note.getVolume();
                byte layerVolume = layerVolumes.get(note.getLayer());
                note.changeVolume((byte) (volume * layerVolume / 100));
            }
        }
    }

    public void addLayerVolume(byte volume) {
        layerVolumes.add(volume);
    }

    public void addlayerVolume(byte volume) {
        this.layerVolumes.add(volume);
    }

    public List<Note> getNotes(int tick) {
        return this.noteMap.get(tick);
    }

    public boolean isNewNBS() {
        return newNBS;
    }

    public void setNewNBS(boolean newNBS) {
        this.newNBS = newNBS;
    }

    public Map<Integer, List<Note>> getNoteMap() {
        return this.noteMap;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "PianoSong{\n" +
                "\tnbsVersion=" + nbsVersion +
                ", \n\tinstrumentCount=" + instrumentCount +
                ", \n\tsongLength=" + length +
                ", \n\tlayerCount=" + layerCount +
                ", \n\tname='" + name + '\'' +
                ", \n\tauthor='" + author + '\'' +
                ", \n\toriginalAuthor='" + originalAuthor + '\'' +
                ", \n\tdescription='" + description + '\'' +
                ", \n\ttempo=" + speed +
                ", \n\tautoSaving=" + autoSaving +
                ", \n\tautoSavingDuration=" + autoSavingDuration +
                ", \n\ttimeSignature=" + timeSignature +
                ", \n\tminutesSpent=" + minutesSpent +
                ", \n\tleftClicks=" + leftClicks +
                ", \n\trightClicks=" + rightClicks +
                ", \n\tnoteBlocksAdded=" + noteBlocksAdded +
                ", \n\tnoteBlocksRemoved=" + noteBlocksRemoved +
                ", \n\tmidiOrSchematicFileName='" + midiOrSchematicFileName + '\'' +
                ", \n\tloop=" + loop +
                ", \n\tmaxLoopCount=" + maxLoopCount +
                ", \n\tloopStartTick=" + loopStartTick +
                ", \n\tnoteMap=" + noteMap +
                "\n}";
    }
}
