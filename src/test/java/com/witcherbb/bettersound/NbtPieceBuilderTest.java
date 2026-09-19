package com.witcherbb.bettersound;

import com.witcherbb.bettersound.world.structure.NbtPieceBuilder;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * 音乐小屋结构池的“模板 -> 权重”收集器。
 */
public class NbtPieceBuilderTest {

    @Test
    public void freshBuilderBuildsAnEmptyMap() {
        assertTrue(new NbtPieceBuilder().build().isEmpty());
    }

    @Test
    public void entriesAreCollectedInInsertionOrder() {
        Map<String, Integer> map = new NbtPieceBuilder()
                .add("bettersound:music_house/house", 1)
                .add("bettersound:music_house/parrot_red", 2)
                .add("bettersound:music_house/parrot_blue", 3)
                .build();

        assertEquals(3, map.size());
        assertEquals(Integer.valueOf(1), map.get("bettersound:music_house/house"));
        assertEquals(Integer.valueOf(3), map.get("bettersound:music_house/parrot_blue"));
    }

    @Test
    public void laterWeightReplacesTheEarlierOneForTheSamePiece() {
        Map<String, Integer> map = new NbtPieceBuilder()
                .add("house", 1)
                .add("house", 5)
                .build();

        assertEquals("同一个模板只保留最后一次的权重", 1, map.size());
        assertEquals(Integer.valueOf(5), map.get("house"));
    }

    @Test
    public void buildReturnsTheLiveMapSoCallersSeeLaterAdds() {
        NbtPieceBuilder builder = new NbtPieceBuilder();
        Map<String, Integer> first = builder.build();
        assertSame(first, builder.build());

        builder.add("house", 1);
        assertEquals(1, first.size());
    }

    @Test
    public void addIsChainable() {
        NbtPieceBuilder builder = new NbtPieceBuilder();
        assertSame(builder, builder.add("a", 1));
    }
}
