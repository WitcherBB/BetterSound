package com.witcherbb.bettersound;

import com.witcherbb.bettersound.music.data.Note;
import com.witcherbb.bettersound.network.MusicScoreTransfer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * 事件表分片与拼装。
 * <p>单个自定义载荷只有 32767 字节，所以这里要保证：每块的估算体积不超预算、切完还能原样拼回来。
 */
public class MusicScoreTransferTest {

    private static Map<Integer, List<Note>> buildScore(int ticks, int notesPerTick) {
        Map<Integer, List<Note>> score = new LinkedHashMap<>();
        for (int tick = 0; tick < ticks; tick++) {
            List<Note> notes = new ArrayList<>(notesPerTick);
            for (int i = 0; i < notesPerTick; i++) {
                notes.add(new Note((byte) ((tick + i) % 88), (byte) 100));
            }
            score.put(tick, notes);
        }
        return score;
    }

    @Test
    public void smallScoreStaysInOneChunk() {
        Map<Integer, List<Note>> score = buildScore(10, 2);
        List<Map<Integer, List<Note>>> chunks = MusicScoreTransfer.split(score);

        assertEquals(1, chunks.size());
        assertEquals(score, chunks.get(0));
    }

    @Test
    public void bigScoreIsSplitUnderBudget() {
        // 每刻 6 个事件 = 6*6+2+2 = 40 字节，2000 刻约 80KB，必然要切成多块
        Map<Integer, List<Note>> score = buildScore(2000, 6);
        List<Map<Integer, List<Note>>> chunks = MusicScoreTransfer.split(score);

        assertTrue("这么大的曲子必须被切开", chunks.size() > 1);
        for (Map<Integer, List<Note>> chunk : chunks) {
            int size = 0;
            for (Map.Entry<Integer, List<Note>> entry : chunk.entrySet()) {
                size += MusicScoreTransfer.sizeOf(entry.getKey(), entry.getValue());
            }
            assertTrue("单块必须留在预算内，实际 " + size, size <= MusicScoreTransfer.CHUNK_BUDGET);
        }
        // 所有刻都还在，且没有重复
        Map<Integer, List<Note>> merged = new HashMap<>();
        for (Map<Integer, List<Note>> chunk : chunks) {
            for (Map.Entry<Integer, List<Note>> entry : chunk.entrySet()) {
                assertNull("同一刻不能出现在两块里", merged.put(entry.getKey(), entry.getValue()));
            }
        }
        assertEquals(score.keySet(), merged.keySet());
    }

    @Test
    public void emptyScoreStillProducesOneChunk() {
        List<Map<Integer, List<Note>>> chunks = MusicScoreTransfer.split(Map.of());
        assertEquals("空曲子也要发一块，否则服务端永远收不齐", 1, chunks.size());
        assertTrue(chunks.get(0).isEmpty());
    }

    @Test
    public void serverAssemblesChunksBackIntoOneScore() {
        UUID sender = UUID.randomUUID();
        Map<Integer, List<Note>> score = buildScore(2000, 6);
        List<Map<Integer, List<Note>>> chunks = MusicScoreTransfer.split(score);

        Map<Integer, List<Note>> merged = null;
        for (int index = 0; index < chunks.size(); index++) {
            Map<Integer, List<Note>> result = MusicScoreTransfer.accept(sender, index, chunks.size(), chunks.get(index));
            if (index < chunks.size() - 1) {
                assertNull("还没收齐时不该返回结果", result);
            } else {
                merged = result;
            }
        }
        assertNotNull("收齐之后必须给出完整事件表", merged);
        assertEquals(score.keySet(), merged.keySet());
        assertSame("事件对象应当原样保留", score.get(0), merged.get(0));
    }

    @Test
    public void newTransferThrowsAwayTheUnfinishedOne() {
        UUID sender = UUID.randomUUID();
        // 第一次投递：说要 3 块，却只发了第 0 块
        assertNull(MusicScoreTransfer.accept(sender, 0, 3, buildScore(2, 1)));
        // 换歌：新投递只有 1 块，应当直接收齐，而且只包含这一次的内容
        Map<Integer, List<Note>> merged = MusicScoreTransfer.accept(sender, 0, 1, buildScore(4, 1));
        assertNotNull(merged);
        assertEquals("上一次没传完的那份必须被丢掉", 4, merged.size());
    }

    /**
     * 半途退游戏会留下半份，服务端不能无限攒着。
     * 这里灌满上限之后再回头看第一份——它应该已经被挤掉了。
     */
    @Test
    public void unfinishedTransfersAreBounded() {
        UUID first = UUID.randomUUID();
        assertNull(MusicScoreTransfer.accept(first, 0, 3, buildScore(2, 1)));
        for (int i = 0; i < 100; i++) {
            MusicScoreTransfer.accept(UUID.randomUUID(), 0, 3, buildScore(1, 1));
        }
        // 被挤掉之后，剩下的两块已经没人能拼起来了
        assertNull(MusicScoreTransfer.accept(first, 1, 3, buildScore(3, 1)));
        assertNull(MusicScoreTransfer.accept(first, 2, 3, buildScore(4, 1)));
    }
}
