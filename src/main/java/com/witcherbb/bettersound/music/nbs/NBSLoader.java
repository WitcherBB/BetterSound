package com.witcherbb.bettersound.music.nbs;

import com.mojang.logging.LogUtils;
import com.witcherbb.bettersound.exception.FileIsNotNBSException;
import com.witcherbb.bettersound.exception.NBSNotFoundException;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NBSLoader {
    private final File dir;
    private final Map<String, PianoSong> songs = new HashMap<>();

    public NBSLoader(File gameDirectory, String dirName) {
        File dir = new File(gameDirectory, dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        } else if (!dir.isDirectory()) throw new IllegalStateException("The file is not directory");
        this.dir = dir;
    }

    public void load() {
        File[] files = this.dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            try {
                NBSReader reader = new NBSReader(file);
                PianoSong song = reader.readPiano();
                songs.put(song.fileName, song);
            } catch (Exception e) {
                if (e.getClass() == FileIsNotNBSException.class) {
                    LogUtils.getLogger().warn(e.getMessage());
                } else {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public PianoSong findSong(String fileName) throws NBSNotFoundException {
        String fileName1 = fileName.endsWith(".nbs") ? fileName : fileName + ".nbs";
        PianoSong song = this.songs.get(fileName1);
        if (song == null) throw new NBSNotFoundException(fileName1);
        return song;
    }

    public Map<String, PianoSong> getSongs() {
        return songs;
    }

    public File getDir() {
        return dir;
    }
}
