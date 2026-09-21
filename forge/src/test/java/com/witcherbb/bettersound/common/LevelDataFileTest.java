package com.witcherbb.bettersound.common;

import com.google.gson.reflect.TypeToken;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import net.minecraft.core.BlockPos;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 存档数据文件（NBT + Codec）的读写。
 *
 * <p>本测试类与 {@link LevelDataFile} 同包，是为了直接注入受保护的 {@code dataFile} 字段：
 * 正常路径下这个字段由 {@link LevelDataFile#create} 依赖服务端目录给出，单测里给不了。</p>
 */
public class LevelDataFileTest {

    private static final Type LIST_OF_JUKEBOX = new TypeToken<List<JukeboxEntityData>>() {}.getType();

    private static File tempDir() {
        File dir = new File("build/dsh-verify/level-data-file");
        assertTrue("测试目录建不出来", dir.exists() || dir.mkdirs());
        return dir;
    }

    private static File freshFile(String name) {
        File file = new File(tempDir(), name);
        if (file.exists()) {
            assertTrue("旧文件删不掉：" + file, file.delete());
        }
        return file;
    }

    private static LevelDataFile<List<JukeboxEntityData>> dataFileOn(File file) {
        LevelDataFile<List<JukeboxEntityData>> dataFile =
                new LevelDataFile<>("jukebox_data", LIST_OF_JUKEBOX, JukeboxEntityData.CODEC.listOf());
        dataFile.dataFile = file;
        return dataFile;
    }

    @Test
    public void writeThenReadGivesBackTheSameData() {
        File file = freshFile("round-trip.nbt");
        LevelDataFile<List<JukeboxEntityData>> dataFile = dataFileOn(file);
        assertEquals("jukebox_data", dataFile.getFileName());

        JukeboxEntityData data = new JukeboxEntityData("music_house", "overworld");
        data.addBlockPos(new BlockPos(1, 2, 3));
        data.addBlockPos(new BlockPos(4, 5, 6));
        data.addControllerPos(new BlockPos(7, 8, 9));

        dataFile.wirteToNbt(List.of(data));

        // wirteToNbt 内部把异常吞了，所以这里用“文件确实写进去东西了”来兜底
        assertTrue("写完之后文件应当非空", file.length() > 0L);

        Optional<List<JukeboxEntityData>> read = dataFile.readFromJson();
        assertTrue("刚写进去的数据应当能读回来", read.isPresent());
        assertEquals(1, read.get().size());
        assertEquals(data, read.get().get(0));
        assertEquals(List.of(new BlockPos(1, 2, 3), new BlockPos(4, 5, 6)), read.get().get(0).getPosList());
        assertEquals(List.of(new BlockPos(7, 8, 9)), read.get().get(0).getControllerPosList());
    }

    @Test
    public void secondWriteReplacesTheFirstOne() {
        File file = freshFile("overwrite.nbt");
        LevelDataFile<List<JukeboxEntityData>> dataFile = dataFileOn(file);

        dataFile.wirteToNbt(List.of(new JukeboxEntityData("first_house", "overworld")));
        dataFile.wirteToNbt(List.of(new JukeboxEntityData("second_house", "overworld")));

        Optional<List<JukeboxEntityData>> read = dataFile.readFromJson();
        assertTrue(read.isPresent());
        assertEquals("重复保存应当是覆盖而不是追加", 1, read.get().size());
        assertEquals("second_house", read.get().get(0).getName());
    }

    @Test
    public void emptyListSurvivesTheRoundTrip() {
        File file = freshFile("empty-list.nbt");
        LevelDataFile<List<JukeboxEntityData>> dataFile = dataFileOn(file);

        dataFile.wirteToNbt(List.of());

        Optional<List<JukeboxEntityData>> read = dataFile.readFromJson();
        assertTrue(read.isPresent());
        assertTrue(read.get().isEmpty());
    }

    @Test
    public void readingAnEmptyFileFailsLoudly() throws Exception {
        File file = freshFile("never-written.nbt");
        assertTrue("建不出空文件", file.createNewFile());
        assertEquals(0L, file.length());

        LevelDataFile<List<JukeboxEntityData>> dataFile = dataFileOn(file);

        try {
            dataFile.readFromJson();
            fail("空文件应当直接报错，而不是悄悄当成空数据");
        } catch (IllegalStateException expected) {
            assertEquals("readFromJson wrong", expected.getMessage());
        }
    }

    @Test
    public void readingAMissingFileFailsLoudly() {
        File file = freshFile("deleted.nbt");
        assertFalse(file.exists());

        LevelDataFile<List<JukeboxEntityData>> dataFile = dataFileOn(file);

        try {
            dataFile.readFromJson();
            fail("文件不存在时应当直接报错");
        } catch (IllegalStateException expected) {
            assertEquals("readFromJson wrong", expected.getMessage());
        }
    }
}
