package com.witcherbb.bettersound.music.nbs.bean;

import com.witcherbb.bettersound.music.MusicTiming;
import com.witcherbb.bettersound.music.bean.Note;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
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
    /** NBS 原始 tempo：每秒歌曲刻数 × 100（1000 即每秒 10 刻），换算见 {@link MusicTiming} */
    public short tempo;
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

    /**
     * 记录一个音符。
     * <p>
     * 键 {@code tick} 就是 NBS 文件里的歌曲刻下标，读取阶段不做任何时间缩放：
     * 缩放（歌曲刻 → 游戏刻）由 {@link MusicTiming} 在播放时按 tempo 精确换算。
     * 以前在这里乘 {@code speed}（floor 过的整数倍率），会让 tempo 不能整除以
     * 游戏刻的曲子整体变快。
     */
    public void addNote(int tick, byte tone, byte volume, short layer) {
        this.noteMap.computeIfAbsent(tick, k -> new ArrayList<>())
                .add(new Note(tone, volume).withLayer((byte) layer));
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

    /** @param tick NBS 歌曲刻下标（不是游戏刻） */
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
                ", \n\ttempo=" + tempo + " (" + MusicTiming.songTicksPerSecond(tempo) + " 歌曲刻/秒, "
                        + MusicTiming.gameTicksPerSongTick(tempo) + " 游戏刻/歌曲刻)" +
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
