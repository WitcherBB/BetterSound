package com.witcherbb.bettersound;

import com.witcherbb.bettersound.blocks.state.properties.ExamplePart;
import com.witcherbb.bettersound.blocks.state.properties.PianoPart;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 多方块的“部位”枚举。序列化名会作为 BlockState 属性值进入存档与同步数据，
 * 改名就等于让老存档里的钢琴认不出来，所以这里把名字与顺序都当成对外契约钉死。
 */
public class BlockPartTest {

    @Test
    public void pianoPartsKeepTheirSerializedNames() {
        assertEquals(
                List.of("pedal", "pedal_right", "keyboard_right", "keyboard_middle", "keyboard_left", "pedal_left"),
                Arrays.stream(PianoPart.values()).map(PianoPart::getSerializedName).collect(Collectors.toList()));
    }

    @Test
    public void pianoPartDeclarationOrderIsStable() {
        // PianoPart 的源码注释写明“按渲染顺序排放”，这里把声明顺序锁住，防止无意间重排
        assertArrayEquals(
                new PianoPart[]{
                        PianoPart.PEDAL, PianoPart.PEDAL_R, PianoPart.KEYBOARD_R,
                        PianoPart.KEYBOARD_M, PianoPart.KEYBOARD_L, PianoPart.PEDAL_L},
                PianoPart.values());
    }

    @Test
    public void examplePartsKeepTheirSerializedNames() {
        assertEquals("first", ExamplePart.FIRST.getSerializedName());
        assertEquals("second", ExamplePart.SECOND.getSerializedName());
        assertEquals(ExamplePart.FIRST.getSerializedName(), ExamplePart.FIRST.getName());
        assertEquals(ExamplePart.SECOND.getSerializedName(), ExamplePart.SECOND.getName());
    }

    @Test
    public void serializedNamesAreUniqueWithinEachEnum() {
        Set<String> piano = new HashSet<>();
        for (PianoPart part : PianoPart.values()) {
            assertTrue("序列化名重复：" + part, piano.add(part.getSerializedName()));
        }
        assertEquals(PianoPart.values().length, piano.size());

        Set<String> example = new HashSet<>();
        for (ExamplePart part : ExamplePart.values()) {
            assertTrue("序列化名重复：" + part, example.add(part.getSerializedName()));
        }
        assertEquals(ExamplePart.values().length, example.size());
    }
}
