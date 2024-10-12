package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.exception.FileIsNotNBSException;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.music.util.BinaryFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class NBSReader {
    private BinaryFileReader reader;
    private final String fileName;

    public NBSReader(String path) throws FileNotFoundException, FileIsNotNBSException {
        this(new File(path));
    }

    public NBSReader(File file) throws FileNotFoundException, FileIsNotNBSException {
        if (!file.getName().endsWith(".nbs")) throw new FileIsNotNBSException(file);
        this.reader = new BinaryFileReader(file);
        this.fileName = file.getName();
    }

    public PianoSong readPiano() throws IOException {
        PianoSong pianoSong = new PianoSong(this.fileName);
        pianoSong.length = reader.readShort();
        pianoSong.setNewNBS(pianoSong.length == 0);
        if (pianoSong.isNewNBS()) {
            pianoSong.nbsVersion = reader.readByte();
            pianoSong.instrumentCount = reader.readByte();
            pianoSong.length = reader.readShort();
        }
        pianoSong.layerCount = reader.readShort();
        pianoSong.name = reader.readString();
        pianoSong.author = reader.readString();
        pianoSong.originalAuthor = reader.readString();
        pianoSong.description = reader.readString();
        pianoSong.speed = (short) Math.floor((double) 20 / ((double) reader.readShort() / 100));
        pianoSong.speed = pianoSong.speed <= 1 ? 1 : pianoSong.speed;
        pianoSong.autoSaving = reader.readByte();
        pianoSong.autoSavingDuration = reader.readByte();
        pianoSong.timeSignature = reader.readByte();
        pianoSong.minutesSpent = reader.readInt();
        pianoSong.leftClicks = reader.readInt();
        pianoSong.rightClicks = reader.readInt();
        pianoSong.noteBlocksAdded = reader.readInt();
        pianoSong.noteBlocksRemoved = reader.readInt();
        pianoSong.midiOrSchematicFileName = reader.readString();
        if (pianoSong.isNewNBS()) {
            pianoSong.loop = reader.readByte();
            pianoSong.maxLoopCount = reader.readByte();
            pianoSong.loopStartTick = reader.readShort();
        }

        int tick = -1;
        short tick_jumps;
        while ((tick_jumps = reader.readShort()) != 0) {
            tick += tick_jumps;
            short layer = -1;
            short layer_jumps;
            while ((layer_jumps = reader.readShort()) != 0) {
                layer += layer_jumps;

                byte instrument = reader.readByte();
                byte tone = reader.readByte();
                byte volume = 100;
                if (pianoSong.isNewNBS()) {
                    volume = reader.readByte();
                    reader.readByte();
                    tone = (byte) (((tone * 100) + reader.readShort()) / 100);
                }
                if (instrument != 0) {
                    pianoSong.addNote(tick, (byte) -1, (byte) 0, layer);
                    continue;
                }
                pianoSong.addNote(tick, tone, volume, layer);
            }
        }
        for (short i = 0; i < pianoSong.layerCount; i++) {
            reader.readString();
            byte layerVolume = 100;
            if (pianoSong.isNewNBS()) {
                reader.readByte();
                layerVolume = reader.readByte();
                reader.readByte();
            }
            pianoSong.addlayerVolume(layerVolume);
        }
        pianoSong.parse();
        return pianoSong;
    }

    public void changeFile(File file) throws FileNotFoundException {
        this.reader = new BinaryFileReader(file);
    }

}
