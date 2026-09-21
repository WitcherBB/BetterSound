package com.witcherbb.bettersound.music.midi;

import com.mojang.logging.LogUtils;
import com.witcherbb.bettersound.exception.MidiNotFoundException;
import com.witcherbb.bettersound.music.midi.bean.MidiSong;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 扫描并缓存 {@code <游戏目录>/midi_bettersound} 下的 MIDI 文件（客户端）。
 * <p>与 NBS 的 {@code NBSLoader} 对应：解析只在客户端做，服务端只会收到编译后的裸数据。
 */
public class MidiLoader {
    private final File dir;
    private final Map<String, MidiSong> songs = new HashMap<>();

    public MidiLoader(File gameDirectory, String dirName) {
        File dir = new File(gameDirectory, dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        } else if (!dir.isDirectory()) throw new IllegalStateException("The file is not directory");
        this.dir = dir;
    }

    public void load() {
        // 先清空：删掉文件后再 reload，不会像 NBSLoader 那样残留旧条目
        this.songs.clear();
        File[] files = this.dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.isFile() || !MidiReader.hasMidiExtension(file.getName())) continue;
            try (MidiReader reader = new MidiReader(file)) {
                MidiSong song = reader.readSong();
                this.songs.put(song.fileName, song);
                LogUtils.getLogger().info("MIDI {}: 曲名={}, 收录 {} 个音符（越界丢弃 {} 个, 打击乐丢弃 {} 个）, 踏板事件 {} 条, 小节 {} 游戏刻",
                        song.fileName, song.name, song.notes.size(), song.outOfRangeNotes, song.drumNotes,
                        song.pedals.size(), song.barGameTicks);
            } catch (Exception e) {
                LogUtils.getLogger().warn("MIDI {} 解析失败: {}", file.getName(), e.getMessage());
            }
        }
    }

    public MidiSong findSong(String fileName) throws MidiNotFoundException {
        MidiSong song = this.songs.get(fileName);
        if (song == null && !MidiReader.hasMidiExtension(fileName)) {
            // 允许省略扩展名
            for (String extension : MidiReader.MIDI_EXTENSIONS) {
                song = this.songs.get(fileName + extension);
                if (song != null) break;
            }
        }
        if (song == null) throw new MidiNotFoundException(fileName);
        return song;
    }

    public Map<String, MidiSong> getSongs() {
        return songs;
    }

    public File getDir() {
        return dir;
    }
}
