package com.witcherbb.bettersound;

import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 唱片机控制器存档数据。它会被 Codec 序列化进存档文件，
 * “名字 + 维度”是它的身份，坐标列表只是附带信息。
 */
public class JukeboxEntityDataTest {

    private static final String NAME = "music_house";
    private static final String DIMENSION = "overworld";
    private static final BlockPos JUKEBOX = new BlockPos(1, 2, 3);
    private static final BlockPos OTHER_JUKEBOX = new BlockPos(4, 5, 6);
    private static final BlockPos CONTROLLER = new BlockPos(7, 8, 9);

    private static JukeboxEntityData data(String name, String dimension) {
        return new JukeboxEntityData(name, dimension);
    }

    @Test
    public void freshDataHasEmptyLists() {
        JukeboxEntityData data = data(NAME, DIMENSION);
        assertEquals(NAME, data.getName());
        assertEquals(DIMENSION, data.getDimension());
        assertTrue(data.getPosList().isEmpty());
        assertTrue(data.getControllerPosList().isEmpty());
    }

    @Test
    public void positionsAreAddedAndRemovedOneByOne() {
        JukeboxEntityData data = data(NAME, DIMENSION);
        data.addBlockPos(JUKEBOX);
        data.addBlockPos(OTHER_JUKEBOX);
        assertEquals(2, data.getPosList().size());

        data.removeBlockPos(JUKEBOX);
        assertEquals(List.of(OTHER_JUKEBOX), data.getPosList());

        data.addControllerPos(CONTROLLER);
        assertEquals(List.of(CONTROLLER), data.getControllerPosList());
        data.removeControllerPos(CONTROLLER);
        assertTrue(data.getControllerPosList().isEmpty());
    }

    @Test
    public void identityIsNamePlusDimension() {
        JukeboxEntityData a = data(NAME, DIMENSION);
        JukeboxEntityData b = data(NAME, DIMENSION);
        // 方块列表不参与相等性：同一台“音乐小屋唱片机”就是同一份数据
        a.addBlockPos(JUKEBOX);

        assertEquals(a, b);
        assertTrue(a.same(NAME, DIMENSION));
        assertFalse(a.same(NAME, "the_nether"));
        assertFalse(a.same("another_house", DIMENSION));

        assertNotEquals(a, data("another_house", DIMENSION));
        assertNotEquals(a, data(NAME, "the_nether"));
        assertNotEquals(a, null);
        assertNotEquals(a, "not a jukebox");
    }

    @Test
    public void hashCodeIsStableWhenContentMatches() {
        JukeboxEntityData a = data(NAME, DIMENSION);
        JukeboxEntityData b = data(NAME, DIMENSION);
        a.addBlockPos(JUKEBOX);
        a.addControllerPos(CONTROLLER);
        b.addBlockPos(JUKEBOX);
        b.addControllerPos(CONTROLLER);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void codecRoundTripKeepsEverything() {
        JukeboxEntityData data = data(NAME, DIMENSION);
        data.addBlockPos(JUKEBOX);
        data.addBlockPos(OTHER_JUKEBOX);
        data.addControllerPos(CONTROLLER);

        Tag tag = JukeboxEntityData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, msg -> fail(msg));
        JukeboxEntityData decoded = JukeboxEntityData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, msg -> fail(msg));

        assertEquals(NAME, decoded.getName());
        assertEquals(DIMENSION, decoded.getDimension());
        assertEquals(List.of(JUKEBOX, OTHER_JUKEBOX), decoded.getPosList());
        assertEquals(List.of(CONTROLLER), decoded.getControllerPosList());
        assertEquals(data, decoded);
    }

    @Test
    public void codecRoundTripGivesBackMutableLists() {
        JukeboxEntityData data = data(NAME, DIMENSION);
        data.addBlockPos(JUKEBOX);

        Tag tag = JukeboxEntityData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, msg -> fail(msg));
        JukeboxEntityData decoded = JukeboxEntityData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, msg -> fail(msg));

        // 解码出来的对象要继续参与增删，列表不能被做成不可变的
        decoded.addBlockPos(OTHER_JUKEBOX);
        decoded.addControllerPos(CONTROLLER);
        assertEquals(2, decoded.getPosList().size());
        assertEquals(1, decoded.getControllerPosList().size());
    }

    @Test
    public void codecRejectsATagMissingRequiredFields() {
        CompoundTag incomplete = new CompoundTag();
        incomplete.putString("name", NAME);

        assertFalse("缺少 dimension/坐标列表时应当解析失败",
                JukeboxEntityData.CODEC.parse(NbtOps.INSTANCE, incomplete).result().isPresent());
    }
}
