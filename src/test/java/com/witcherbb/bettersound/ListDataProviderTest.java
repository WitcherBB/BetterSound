package com.witcherbb.bettersound;

import com.google.gson.reflect.TypeToken;
import com.witcherbb.bettersound.common.data.ListDataProvider;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import net.minecraft.core.BlockPos;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * 列表型存档数据的通用基类（{@code AbstractJukeboxDataProvider} 是它的兄弟实现）。
 * 重点是 {@code setData} 必须拷贝一份，否则外部拿着同一个 List 一改就把存档改脏了。
 */
public class ListDataProviderTest {

    private static final String NAME = "music_house";
    private static final String DIMENSION = "overworld";

    private static final class TestProvider extends ListDataProvider<JukeboxEntityData> {
        private TestProvider() {
            super("test_data", new TypeToken<List<JukeboxEntityData>>() {}.getType(), JukeboxEntityData.CODEC);
        }
    }

    private static JukeboxEntityData data(String name, String dimension) {
        return new JukeboxEntityData(name, dimension);
    }

    @Test
    public void freshProviderStartsEmpty() {
        TestProvider provider = new TestProvider();
        assertTrue(provider.getData().isEmpty());
    }

    @Test
    public void addAndRemoveKeepTheListInOrder() {
        TestProvider provider = new TestProvider();
        JukeboxEntityData first = data("first", DIMENSION);
        JukeboxEntityData second = data("second", DIMENSION);

        provider.addData(first);
        provider.addData(second);
        assertEquals(List.of(first, second), provider.getData());

        provider.removeData(first);
        assertEquals(List.of(second), provider.getData());
    }

    @Test
    public void existsUsesElementEquality() {
        TestProvider provider = new TestProvider();
        provider.addData(data(NAME, DIMENSION));

        assertTrue(provider.exists(data(NAME, DIMENSION)));
        assertFalse(provider.exists(data(NAME, "the_nether")));
        assertFalse(provider.exists(data("another_house", DIMENSION)));
    }

    @Test
    public void setDataCopiesTheGivenList() {
        TestProvider provider = new TestProvider();
        List<JukeboxEntityData> source = new ArrayList<>();
        source.add(data(NAME, DIMENSION));

        provider.setData(source);
        assertNotSame("不能直接持有调用方的列表", source, provider.getData());
        assertEquals(1, provider.getData().size());

        source.add(data("added_later", DIMENSION));
        assertEquals("外部往源列表里加东西，不该影响 provider", 1, provider.getData().size());
        assertEquals(2, source.size());

        provider.getData().add(data("added_inside", DIMENSION));
        assertEquals("provider 内部加东西，也不该影响调用方手里的列表", 2, source.size());
        assertEquals(2, provider.getData().size());
    }
}
