package com.witcherbb.bettersound;

import com.witcherbb.bettersound.client.gui.PianoUtil;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 88 键钢琴的键位索引与音名。索引同时被键盘映射、界面与配置复用，
 * 音名则会出现在界面上，因此这两件事都要钉死。
 */
public class PianoUtilTest {

    private static final int KEY_COUNT = 88;

    @Test
    public void pianoHas52WhiteAnd36BlackKeys() {
        int black = 0;
        for (int id = 0; id < KEY_COUNT; id++) {
            if (PianoUtil.isBlackey(id)) black++;
        }
        assertEquals("标准钢琴是 36 黑键", 36, black);
        assertEquals("标准钢琴是 52 白键", 52, KEY_COUNT - black);
    }

    @Test
    public void blackKeysFollowTheRealPianoLayout() {
        // A0 是 88 键里唯一一个“下面没有白键”的黑键，单独列出来
        assertTrue("A#0", PianoUtil.isBlackey(1));
        assertFalse("A0 是白键", PianoUtil.isBlackey(0));

        // 其余每个八度（从 C 起算）的黑键相对该八度 C 的偏移固定是 {1,3,6,8,10}
        for (int octaveStart = 3; octaveStart <= 75; octaveStart += 12) {
            for (int offset : new int[]{1, 3, 6, 8, 10}) {
                int id = octaveStart + offset;
                assertTrue("索引 " + id + " 应当是黑键", PianoUtil.isBlackey(id));
            }
            for (int offset : new int[]{0, 2, 4, 5, 7, 9, 11}) {
                int id = octaveStart + offset;
                assertFalse("索引 " + id + " 应当是白键", PianoUtil.isBlackey(id));
            }
        }
        assertFalse("最高音 C8 是白键", PianoUtil.isBlackey(87));
    }

    /** 黑白键与音名必须自洽：带升号的名字只可能出现在黑键上。 */
    @Test
    public void sharpNamesAndBlackKeysAgree() {
        for (int id = 0; id < KEY_COUNT; id++) {
            boolean nameHasSharp = PianoUtil.getKeyName(id).getString().contains("#");
            assertEquals("索引 " + id + " 的黑白键与音名对不上", PianoUtil.isBlackey(id), nameHasSharp);
        }
    }

    @Test
    public void keyNamesAreUniqueAndUseScientificPitchNotation() {
        Set<String> names = new HashSet<>();
        for (int id = 0; id < KEY_COUNT; id++) {
            assertTrue("音名重复：索引 " + id, names.add(PianoUtil.getKeyName(id).getString()));
        }
        assertEquals(KEY_COUNT, names.size());

        assertEquals("A0", PianoUtil.getKeyName(0).getString());
        assertEquals("A#/Bb0", PianoUtil.getKeyName(1).getString());
        assertEquals("B0", PianoUtil.getKeyName(2).getString());
        assertEquals("C1", PianoUtil.getKeyName(3).getString());
        // 索引 39 就是中央 C（对应 MIDI 60），音名必须是 C4
        assertEquals("C4", PianoUtil.getKeyName(39).getString());
        assertEquals("C8", PianoUtil.getKeyName(87).getString());
    }

    @Test
    public void outOfRangeIdsAreRejected() {
        assertEquals("Invalid key ID", PianoUtil.getKeyName(-1).getString());
        assertEquals("Invalid key ID", PianoUtil.getKeyName(KEY_COUNT).getString());
        assertEquals("Invalid key ID", PianoUtil.getKeyName(Integer.MIN_VALUE).getString());
        assertEquals("Invalid key ID", PianoUtil.getKeyName(Integer.MAX_VALUE).getString());
    }
}
