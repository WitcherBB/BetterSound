package com.witcherbb.bettersound.network;

import com.witcherbb.bettersound.music.data.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 把一首曲子的事件表切成能装进单个自定义载荷的若干块，并在服务端按发送者拼回去。
 *
 * <p>单个自定义载荷有 <b>32767 字节</b>上限（{@code Payload may not be larger than 32767 bytes}），
 * 而一首几分钟的曲子上万条事件很常见——「整张表塞进一个包」必然炸。这里按刻切块：
 * 每块只带自己那段刻的事件，服务端收齐之后拼回一张表再开播。
 */
public final class MusicScoreTransfer {
    /**
     * 每块的字节预算。
     * <p>上限 32767 里还要给包头（方块坐标、曲名、若干 varint/boolean）留余量，保守取 20000。
     */
    public static final int CHUNK_BUDGET = 20_000;

    /** 一个 varint 在线上最多占几个字节（只用来估算，宁大勿小） */
    private static int varIntSize(int value) {
        if (value < 0) return 5;
        if (value < 1 << 7) return 1;
        if (value < 1 << 14) return 2;
        if (value < 1 << 21) return 3;
        if (value < 1 << 28) return 4;
        return 5;
    }

    /** 估算某一刻在线上占多少字节：varint 刻号 + varint 事件数 + 每条事件定长 {@code Note.FIELD_NUM} 字节 */
    public static int sizeOf(int tick, List<Note> notes) {
        return varIntSize(tick) + varIntSize(notes.size()) + notes.size() * Note.FIELD_NUM;
    }

    /**
     * 把事件表按刻切成若干块，每块的估算体积不超过 {@link #CHUNK_BUDGET}。
     * <p>一刻的事件不会被拆开（拆开就要额外维护顺序），所以单刻事件特别多时可能略超预算——那也只能让它超。
     *
     * @return 至少一个元素；空曲子会返回一个空表，好让服务端把「收齐」的判定走完
     */
    public static List<Map<Integer, List<Note>>> split(Map<Integer, List<Note>> noteMap) {
        List<Integer> ticks = new ArrayList<>(noteMap.keySet());
        Collections.sort(ticks);

        List<Map<Integer, List<Note>>> chunks = new ArrayList<>();
        Map<Integer, List<Note>> current = new LinkedHashMap<>();
        int used = 0;
        for (Integer tick : ticks) {
            List<Note> notes = noteMap.get(tick);
            int size = sizeOf(tick, notes);
            if (!current.isEmpty() && used + size > CHUNK_BUDGET) {
                chunks.add(current);
                current = new LinkedHashMap<>();
                used = 0;
            }
            current.put(tick, notes);
            used += size;
        }
        if (!current.isEmpty()) chunks.add(current);
        if (chunks.isEmpty()) chunks.add(Map.of());
        return chunks;
    }

    // ---------------- 服务端拼装 ----------------

    /** 一份正在接收的曲子 */
    private static final class IncomingScore {
        private final int chunkCount;
        private final Map<Integer, List<Note>> merged = new HashMap<>();
        private int received;

        private IncomingScore(int chunkCount) {
            this.chunkCount = Math.max(1, chunkCount);
        }
    }

    /**
     * 最多同时保留多少份"正在接收"。唱到一半退游戏、换歌都会留下半份，
     * 一个半份最多攒到一块的量（几 KB～二十 KB），所以给个上限而不是无限攒。
     */
    private static final int MAX_PENDING = 64;

    /** 按发送者区分的接收进度；只在主线程（数据包的 queue 回调）里访问，所以不需要并发容器 */
    private static final Map<UUID, IncomingScore> INCOMING = new LinkedHashMap<>();

    /**
     * 收下一块。
     *
     * @return 收齐之后的完整事件表；还没收齐返回 {@code null}
     */
    public static Map<Integer, List<Note>> accept(UUID sender, int chunkIndex, int chunkCount,
                                                  Map<Integer, List<Note>> part) {
        IncomingScore score = INCOMING.get(sender);
        if (score == null || chunkIndex == 0) {
            // 新的一次投递：从头开始，顺手把上一条没传完的丢掉
            score = new IncomingScore(chunkCount);
            INCOMING.put(sender, score);
            trimPending();
        }
        score.merged.putAll(part);
        score.received++;
        if (score.received < score.chunkCount) return null;
        INCOMING.remove(sender);
        return score.merged;
    }

    /** 超出上限时按投递先后丢掉最老的半份 */
    private static void trimPending() {
        while (INCOMING.size() > MAX_PENDING) {
            Iterator<UUID> it = INCOMING.keySet().iterator();
            it.next();
            it.remove();
        }
    }

    private MusicScoreTransfer() {
    }
}
