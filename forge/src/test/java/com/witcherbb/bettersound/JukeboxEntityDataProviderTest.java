package com.witcherbb.bettersound;

import com.witcherbb.bettersound.common.data.impl.JukeboxEntityDataProvider;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import net.minecraft.core.BlockPos;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * 存档里所有唱片机（按“名字 + 维度”分组）的索引。
 * 关注两件事：同名同维度不会重复登记；控制器全部移除后整条记录要一起消失。
 */
public class JukeboxEntityDataProviderTest {

    private static final String NAME = "music_house";
    private static final String DIMENSION = "overworld";
    private static final String NETHER = "the_nether";
    private static final BlockPos JUKEBOX = new BlockPos(1, 2, 3);
    private static final BlockPos CONTROLLER = new BlockPos(4, 5, 6);

    private static JukeboxEntityData data(String name, String dimension) {
        return new JukeboxEntityData(name, dimension);
    }

    @Test
    public void freshProviderIsEmpty() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();

        assertFalse(provider.exists(NAME, DIMENSION));
        assertNull(provider.get(NAME, DIMENSION));
        assertTrue(provider.getData().isEmpty());
        assertTrue(provider.getPosListByNameAndDimension(NAME, DIMENSION).isEmpty());
        assertTrue(provider.getControllerPosListByNameAndDimension(NAME, DIMENSION).isEmpty());
    }

    @Test
    public void getReturnsTheRegisteredInstance() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        JukeboxEntityData registered = data(NAME, DIMENSION);
        provider.addData(registered);

        assertSame(registered, provider.get(NAME, DIMENSION));
        assertTrue(provider.exists(registered));
        assertTrue(provider.exists(new JukeboxEntityData(NAME, DIMENSION)));
        assertFalse(provider.exists(new JukeboxEntityData(NAME, NETHER)));
    }

    @Test
    public void duplicateNameAndDimensionIsIgnored() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        provider.addData(data(NAME, DIMENSION));

        JukeboxEntityData duplicate = data(NAME, DIMENSION);
        duplicate.addBlockPos(JUKEBOX);
        provider.addData(duplicate);

        assertEquals("同名同维度只能有一份数据", 1, provider.getData().size());
        assertTrue(provider.getPosListByNameAndDimension(NAME, DIMENSION).isEmpty());
    }

    @Test
    public void sameNameInAnotherDimensionIsADifferentJukebox() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        provider.addData(data(NAME, DIMENSION));
        provider.addData(data(NAME, NETHER));

        assertEquals(2, provider.getData().size());
        assertTrue(provider.exists(NAME, NETHER));
    }

    @Test
    public void positionsOnlyLandOnKnownJukeboxes() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();

        provider.addPos(NAME, DIMENSION, JUKEBOX);
        assertTrue("未登记过的名字不该凭空建条目", provider.getPosListByNameAndDimension(NAME, DIMENSION).isEmpty());

        provider.addData(data(NAME, DIMENSION));
        provider.addPos(NAME, DIMENSION, JUKEBOX);
        provider.addPos(NAME, DIMENSION, CONTROLLER);
        assertEquals(List.of(JUKEBOX, CONTROLLER), provider.getPosListByNameAndDimension(NAME, DIMENSION));
    }

    @Test
    public void controllerPositionsAreDeduplicated() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        provider.addData(data(NAME, DIMENSION));
        assertTrue(provider.getControllerPosListByNameAndDimension(NAME, DIMENSION).isEmpty());

        provider.addControllerPos(NAME, DIMENSION, CONTROLLER);
        provider.addControllerPos(NAME, DIMENSION, CONTROLLER);
        assertEquals("同一个控制器只登记一次", 1, provider.getControllerPosListByNameAndDimension(NAME, DIMENSION).size());

        provider.addControllerPos("unknown", DIMENSION, CONTROLLER);
        assertTrue(provider.getControllerPosListByNameAndDimension("unknown", DIMENSION).isEmpty());
    }

    @Test
    public void addDataWithControllerRegistersBoth() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        provider.addData(data(NAME, DIMENSION), CONTROLLER);

        assertTrue(provider.exists(NAME, DIMENSION));
        assertEquals(List.of(CONTROLLER), provider.getControllerPosListByNameAndDimension(NAME, DIMENSION));
    }

    @Test
    public void removingTheLastControllerDropsTheWholeEntry() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        provider.addData(data(NAME, DIMENSION), JUKEBOX);
        provider.addControllerPos(NAME, DIMENSION, CONTROLLER);
        assertEquals(2, provider.getControllerPosListByNameAndDimension(NAME, DIMENSION).size());

        assertFalse("还剩别的控制器，条目不能删", provider.removeControllerPos(NAME, DIMENSION, JUKEBOX));
        assertTrue(provider.exists(NAME, DIMENSION));
        assertEquals(List.of(CONTROLLER), provider.getControllerPosListByNameAndDimension(NAME, DIMENSION));

        assertTrue("最后一个控制器移除后整条记录都该消失", provider.removeControllerPos(NAME, DIMENSION, CONTROLLER));
        assertFalse(provider.exists(NAME, DIMENSION));
        assertTrue(provider.getData().isEmpty());
    }

    @Test
    public void removingFromMissingOrControllerlessEntriesReportsFalse() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        assertFalse(provider.removeControllerPos(NAME, DIMENSION, CONTROLLER));

        provider.addData(data(NAME, DIMENSION));
        assertFalse("没有控制器时不该把条目删掉", provider.removeControllerPos(NAME, DIMENSION, CONTROLLER));
        assertTrue(provider.exists(NAME, DIMENSION));
        assertFalse(provider.removeControllerPos("unknown", DIMENSION, CONTROLLER));
    }

    @Test
    public void unknownLookupsGiveEmptyListsInsteadOfNull() {
        JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
        assertEquals(List.of(), provider.getPosListByNameAndDimension(NAME, DIMENSION));
        assertEquals(List.of(), provider.getControllerPosListByNameAndDimension(NAME, DIMENSION));
    }
}
