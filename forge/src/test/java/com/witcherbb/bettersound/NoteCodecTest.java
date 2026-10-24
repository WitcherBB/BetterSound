package com.witcherbb.bettersound;

import com.witcherbb.bettersound.music.data.Note;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link Note} 自己的定长编解码。
 * <p>时长是 16 位带符号数，哨兵值 {@link Note#DURATION_UNKNOWN} 是负数——
 * 高字节的符号处理错了会把「未知」变成 65535，所以单独钉一遍。
 */
public class NoteCodecTest {

    @Test
    public void roundTripsAllFields() {
        Note note = new Note((byte) 39, (byte) 100).withLayer((byte) 2).withDuration((short) 1234);

        Note decoded = Note.decode(note.encode());

        assertEquals(39, decoded.getPitch());
        assertEquals(100, decoded.getVolume());
        assertEquals(2, decoded.getLayer());
        assertEquals(1234, decoded.getDuration());
        assertTrue(decoded.hasDuration());
    }

    @Test
    public void roundTripsUnknownDurationSentinel() {
        Note note = new Note((byte) 0, (byte) 1);   // 默认就是未知

        assertEquals(Note.DURATION_UNKNOWN, note.getDuration());
        assertFalse(note.hasDuration());

        Note decoded = Note.decode(note.encode());
        assertEquals(Note.DURATION_UNKNOWN, decoded.getDuration());
        assertFalse(decoded.hasDuration());
    }

    @Test
    public void roundTripsBoundaryDurations() {
        short[] values = {0, 1, 255, 256, 32767};
        for (short value : values) {
            Note decoded = Note.decode(new Note((byte) 0, (byte) 1).withDuration(value).encode());
            assertEquals("duration " + value, value, decoded.getDuration());
        }
    }

    @Test
    public void roundTripsEventKinds() {
        assertEquals(Note.KIND_PLAY, Note.decode(new Note((byte) 1, (byte) 2).encode()).getKind());
        assertEquals(Note.KIND_RELEASE, Note.decode(Note.release((byte) 39).encode()).getKind());
        assertEquals(Note.KIND_RELEASE_ALL, Note.decode(Note.releaseAll().encode()).getKind());
        assertEquals(Note.KIND_PEDAL_DOWN, Note.decode(Note.pedalDown().encode()).getKind());
        assertEquals(Note.KIND_PEDAL_UP, Note.decode(Note.pedalUp().encode()).getKind());
        assertTrue(Note.pedalUp().isPedal());
        assertFalse("踏板事件不是释放事件", Note.pedalUp().isRelease());
    }

    @Test
    public void encodedLengthMatchesFieldNum() {
        assertEquals(Note.FIELD_NUM, new Note((byte) 0, (byte) 0).encode().length);
    }
}
