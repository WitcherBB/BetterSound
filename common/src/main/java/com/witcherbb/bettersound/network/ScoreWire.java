package com.witcherbb.bettersound.network;

import com.witcherbb.bettersound.music.data.Note;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 曲目事件表的线上编解码。
 *
 * <p>格式是紧凑的：{@code varint 刻数} + 每刻 {@code varint 刻号 / varint 事件数 / 每条事件定长 FIELD_NUM 字节}。
 * 以前用 {@code writeMap} + 逐条 {@code writeByteArray}，每条事件要多背一个长度前缀，刻号也用 int，
 * 一首歌下来白白多出三成体积——正好卡在 32767 的限制边缘。
 */
public final class ScoreWire {

    public static void writeNotes(FriendlyByteBuf buf, Map<Integer, List<Note>> noteMap) {
        buf.writeVarInt(noteMap.size());
        for (Map.Entry<Integer, List<Note>> entry : noteMap.entrySet()) {
            buf.writeVarInt(entry.getKey());
            List<Note> notes = entry.getValue();
            buf.writeVarInt(notes.size());
            for (int i = 0; i < notes.size(); i++) {
                buf.writeBytes(notes.get(i).encode());
            }
        }
    }

    public static Map<Integer, List<Note>> readNotes(FriendlyByteBuf buf) {
        int tickCount = buf.readVarInt();
        Map<Integer, List<Note>> noteMap = new HashMap<>(Math.max(1, tickCount));
        for (int tickIndex = 0; tickIndex < tickCount; tickIndex++) {
            int tick = buf.readVarInt();
            int size = buf.readVarInt();
            List<Note> notes = new ArrayList<>(Math.max(1, size));
            for (int i = 0; i < size; i++) {
                byte[] raw = new byte[Note.FIELD_NUM];
                buf.readBytes(raw);
                notes.add(Note.decode(raw));
            }
            noteMap.put(tick, notes);
        }
        return noteMap;
    }

    private ScoreWire() {
    }
}
