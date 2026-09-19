package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.exception.FileIsNotNBSException;
import com.witcherbb.bettersound.music.MusicTiming;
import com.witcherbb.bettersound.music.nbs.bean.PianoSong;
import com.witcherbb.bettersound.music.util.BinaryFileReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class NBSReader implements Closeable {
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
        // tempo 字段单位是「每秒歌曲刻数 × 100」，这里原样保留、不做任何取整：
        // 以前在这里折算成整数倍率 floor(20 / (tempo / 100))，会把 16 刻/秒（1.25 游戏刻/歌曲刻）
        // 压成 1，整首歌直接快 25%。歌曲刻 → 游戏刻 的换算改由 MusicTiming 在播放时精确进行。
        short tempo = reader.readShort();
        pianoSong.tempo = tempo > 0 ? tempo : MusicTiming.NBS_DEFAULT_TEMPO;
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
                // 非钢琴乐器（鼓、贝斯等）无法由钢琴演奏，直接跳过。
                // 不再插入 tone=-1 的哨兵音符：它会随网络包发给客户端，
                // 导致 pianoSounds.get(-1) 越界崩溃。
                if (instrument == 0) {
                    pianoSong.addNote(tick, tone, volume, layer);
                }
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

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
