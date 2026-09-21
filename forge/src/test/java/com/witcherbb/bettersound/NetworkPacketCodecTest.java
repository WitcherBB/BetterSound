package com.witcherbb.bettersound;

import com.witcherbb.bettersound.common.utils.Util;
import com.witcherbb.bettersound.music.bean.Note;
import com.witcherbb.bettersound.network.protocol.client.CJukeboxNameConfirmPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CCommandPlayNBSPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSReloadPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSStopPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockPlayMultipleNotesPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockStopPacket;
import com.witcherbb.bettersound.network.protocol.server.SBlockEntityDataChangePacket;
import com.witcherbb.bettersound.network.protocol.server.SExampleNameChangedPacket;
import com.witcherbb.bettersound.network.protocol.server.SJukeboxControllerNamePacket;
import com.witcherbb.bettersound.network.protocol.server.SJukeboxNamePacket;
import com.witcherbb.bettersound.network.protocol.server.SNoteBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.server.midi.SMidiPlayPacket;
import com.witcherbb.bettersound.network.protocol.server.nbs.SAutoPlayerActionPacket;
import com.witcherbb.bettersound.network.protocol.server.nbs.SNBSPlayPacket;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyPressedPacket;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyReleasedPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 全部网络包的编解码往返。
 *
 * <p>这些包没有版本号也没有回退路径，encode/decode 一旦不对称（多写一个字段、
 * 宽窄类型写反、把可空字段写成必填）在实机上就是“连上服务器就掉线”，
 * 而且很难从日志里看出来。这里逐个做“编码 -> 解码 -> 字段比对”，
 * 并且默认要求解码把编码写下的字节正好读完——这正是最容易出问题的地方。</p>
 */
public class NetworkPacketCodecTest {

    private static final BlockPos POS = new BlockPos(12, 70, -34);

    private static FriendlyByteBuf buffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    private static <P> P encodeDecode(P packet, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder) {
        FriendlyByteBuf buf = buffer();
        encoder.accept(packet, buf);
        return decoder.apply(buf);
    }

    /** 往返并额外要求“字节正好读完”，多写少读都会在这里炸出来。 */
    private static <P> P roundTrip(P packet, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder) {
        FriendlyByteBuf buf = buffer();
        encoder.accept(packet, buf);
        P decoded = decoder.apply(buf);
        assertEquals(packet.getClass().getSimpleName() + " 解码后还剩了没读完的字节", 0, buf.readableBytes());
        return decoded;
    }

    private static void assertNoteEquals(Note expected, Note actual) {
        assertEquals("pitch", expected.getPitch(), actual.getPitch());
        assertEquals("volume", expected.getVolume(), actual.getVolume());
        assertEquals("layer", expected.getLayer(), actual.getLayer());
    }

    /** Note 没有 equals，音符表只能逐个字段比对。 */
    private static void assertNoteMapEquals(Map<Integer, List<Note>> expected, Map<Integer, List<Note>> actual) {
        assertEquals("时刻集合", expected.keySet(), actual.keySet());
        for (Map.Entry<Integer, List<Note>> entry : expected.entrySet()) {
            List<Note> expectedNotes = entry.getValue();
            List<Note> actualNotes = actual.get(entry.getKey());
            assertEquals("第 " + entry.getKey() + " 刻的音符数", expectedNotes.size(), actualNotes.size());
            for (int i = 0; i < expectedNotes.size(); i++) {
                assertNoteEquals(expectedNotes.get(i), actualNotes.get(i));
            }
        }
    }

    private static Map<Integer, List<Note>> sampleNoteMap() {
        Map<Integer, List<Note>> noteMap = new LinkedHashMap<>();
        noteMap.put(0, List.of(new Note((byte) 39, (byte) 100)));
        noteMap.put(20, List.of(new Note((byte) 41, (byte) 90), new Note((byte) 43, (byte) 80).withLayer((byte) 2)));
        return noteMap;
    }

    // ---------------------------------------------------------------- 服务端 -> 客户端

    @Test
    public void blockEntityDataChange() {
        SBlockEntityDataChangePacket packet = new SBlockEntityDataChangePacket(POS, true);
        assertEquals(packet, roundTrip(packet, SBlockEntityDataChangePacket::encode, SBlockEntityDataChangePacket::decode));
    }

    @Test
    public void blockEntityDataChangeKeepsTheDelayFlag() {
        assertTrue(roundTrip(new SBlockEntityDataChangePacket(POS, true),
                SBlockEntityDataChangePacket::encode, SBlockEntityDataChangePacket::decode).delay());
        assertFalse(roundTrip(new SBlockEntityDataChangePacket(POS, false),
                SBlockEntityDataChangePacket::encode, SBlockEntityDataChangePacket::decode).delay());
    }

    @Test
    public void noteBlockPlayNoteCoversTheWholeNoteBlockRange() {
        // 音符盒的音高是 0..24
        for (int note = 0; note <= 24; note++) {
            SNoteBlockPlayNotePacket packet = new SNoteBlockPlayNotePacket(note, POS);
            assertEquals("音高 " + note, packet, roundTrip(packet, SNoteBlockPlayNotePacket::encode, SNoteBlockPlayNotePacket::decode));
        }
    }

    @Test
    public void pianoKeyPressed() {
        SPianoKeyPressedPacket packet = new SPianoKeyPressedPacket(POS, 39, (byte) 100);
        assertEquals(packet, roundTrip(packet, SPianoKeyPressedPacket::encode, SPianoKeyPressedPacket::decode));
    }

    @Test
    public void pianoKeyReleasedCoversTheWholePianoRange() {
        // tone 走的是 writeByte/readByte，88 键的最高音 87 正好落在 byte 正区间里
        for (int tone = 0; tone < 88; tone++) {
            SPianoKeyReleasedPacket packet = new SPianoKeyReleasedPacket(POS, tone, tone % 2 == 0);
            assertEquals("音调 " + tone, packet, roundTrip(packet, SPianoKeyReleasedPacket::encode, SPianoKeyReleasedPacket::decode));
        }
    }

    @Test
    public void jukeboxName() {
        SJukeboxNamePacket packet = new SJukeboxNamePacket("music_house", "overworld");
        assertEquals(packet, roundTrip(packet, SJukeboxNamePacket::encode, SJukeboxNamePacket::decode));
    }

    @Test
    public void jukeboxControllerNameKeepsNonAsciiText() {
        SJukeboxControllerNamePacket packet = new SJukeboxControllerNamePacket("音乐小屋", "下界");
        assertEquals(packet, roundTrip(packet, SJukeboxControllerNamePacket::encode, SJukeboxControllerNamePacket::decode));
    }

    @Test
    public void autoPlayerActionCoversEveryAction() {
        for (SAutoPlayerActionPacket.Action action : SAutoPlayerActionPacket.Action.values()) {
            SAutoPlayerActionPacket packet = new SAutoPlayerActionPacket(POS, action);
            assertEquals("动作 " + action, packet,
                    roundTrip(packet, SAutoPlayerActionPacket::encode, SAutoPlayerActionPacket::decode));
        }
    }

    @Test
    public void autoPlayerActionIdsFollowDeclarationOrder() {
        assertEquals(SAutoPlayerActionPacket.Action.PLAY_ON, SAutoPlayerActionPacket.Action.fromId(0));
        assertEquals(SAutoPlayerActionPacket.Action.STOP, SAutoPlayerActionPacket.Action.fromId(1));
        assertEquals(SAutoPlayerActionPacket.Action.PAUSE, SAutoPlayerActionPacket.Action.fromId(2));
    }

    @Test
    public void nbsPlayKeepsEveryNoteAndTheTempo() {
        Map<Integer, List<Note>> noteMap = sampleNoteMap();
        SNBSPlayPacket packet = new SNBSPlayPacket(POS, "school_song", noteMap, (short) 2000, (byte) 4);

        SNBSPlayPacket decoded = roundTrip(packet, SNBSPlayPacket::encode, SNBSPlayPacket::decode);

        assertEquals(POS, decoded.pos());
        assertEquals("school_song", decoded.name());
        assertEquals((short) 2000, decoded.tempo());
        assertEquals((byte) 4, decoded.timeSignature());
        assertNoteMapEquals(noteMap, decoded.noteMap());
    }

    @Test
    public void midiPlayKeepsNotesAndThePedalTimeline() {
        Map<Integer, List<Note>> noteMap = sampleNoteMap();
        Map<Integer, Boolean> pedalChanges = new LinkedHashMap<>();
        pedalChanges.put(0, Boolean.TRUE);
        pedalChanges.put(20, Boolean.FALSE);
        SMidiPlayPacket packet = new SMidiPlayPacket(POS, "see_you_again", noteMap, 40, pedalChanges);

        SMidiPlayPacket decoded = roundTrip(packet, SMidiPlayPacket::encode, SMidiPlayPacket::decode);

        assertEquals(POS, decoded.pos());
        assertEquals("see_you_again", decoded.name());
        assertEquals(40, decoded.subsectionLength());
        assertNoteMapEquals(noteMap, decoded.noteMap());
        assertEquals(pedalChanges, decoded.pedalChanges());
    }

    @Test
    public void midiPlayWithoutPedalTimeline() {
        SMidiPlayPacket packet = new SMidiPlayPacket(POS, "no_pedal", sampleNoteMap(), 40, Map.of());
        SMidiPlayPacket decoded = roundTrip(packet, SMidiPlayPacket::encode, SMidiPlayPacket::decode);
        assertTrue("没有踏板时间轴时要原样回来，不能变成 null", decoded.pedalChanges().isEmpty());
    }

    /** 这个包的字符串是“裸 UTF-8 字节 + 读到缓冲区末尾”，所以额外单独验证一次。 */
    @Test
    public void exampleNameChangedCarriesRawUtf8() {
        SExampleNameChangedPacket packet = new SExampleNameChangedPacket("音乐小屋 example");
        assertEquals(packet, encodeDecode(packet, SExampleNameChangedPacket::encode, SExampleNameChangedPacket::decode));
    }

    // ---------------------------------------------------------------- 客户端 -> 服务端

    @Test
    public void jukeboxNameConfirmCoversEveryStatus() {
        // writeEnum/readEnum 走的是 ordinal，Status 里 FAIL 与 NULL 数值相同但序号不同，
        // 四个常量都必须能原样往返
        for (Util.Status status : Util.Status.values()) {
            CJukeboxNameConfirmPacket packet = new CJukeboxNameConfirmPacket(status);
            assertEquals("状态 " + status, packet,
                    roundTrip(packet, CJukeboxNameConfirmPacket::encode, CJukeboxNameConfirmPacket::decode));
        }
    }

    @Test
    public void nbsPlayOn() {
        CNBSPlayOnPacket packet = new CNBSPlayOnPacket(POS);
        assertEquals(packet, roundTrip(packet, CNBSPlayOnPacket::encode, CNBSPlayOnPacket::decode));
    }

    @Test
    public void nbsStop() {
        CNBSStopPacket packet = new CNBSStopPacket(POS);
        assertEquals(packet, roundTrip(packet, CNBSStopPacket::encode, CNBSStopPacket::decode));
    }

    @Test
    public void nbsPause() {
        CNBSPausePacket packet = new CNBSPausePacket(POS);
        assertEquals(packet, roundTrip(packet, CNBSPausePacket::encode, CNBSPausePacket::decode));
    }

    @Test
    public void nbsReloadCarriesNothing() {
        FriendlyByteBuf buf = buffer();
        new CNBSReloadPacket().encode(buf);
        assertEquals("这个包不该占任何字节", 0, buf.readableBytes());
        assertEquals(new CNBSReloadPacket(), CNBSReloadPacket.decode(buf));
    }

    @Test
    public void commandPlayNbs() {
        CCommandPlayNBSPacket packet = new CCommandPlayNBSPacket("school_song", POS);
        assertEquals(packet, roundTrip(packet, CCommandPlayNBSPacket::encode, CCommandPlayNBSPacket::decode));
    }

    @Test
    public void pianoBlockStopKeepsTheWholeToneArray() {
        int[] tones = {0, 39, 87};
        CPianoBlockStopPacket packet = new CPianoBlockStopPacket(POS, tones);

        CPianoBlockStopPacket decoded = roundTrip(packet, CPianoBlockStopPacket::encode, CPianoBlockStopPacket::decode);

        assertEquals(POS, decoded.pos());
        assertArrayEquals(tones, decoded.tones());
    }

    @Test
    public void pianoBlockStopWithNoTones() {
        CPianoBlockStopPacket packet = new CPianoBlockStopPacket(POS, new int[0]);
        CPianoBlockStopPacket decoded = roundTrip(packet, CPianoBlockStopPacket::encode, CPianoBlockStopPacket::decode);
        assertEquals(0, decoded.tones().length);
    }

    @Test
    public void pianoBlockPlayNoteRoundTripsIncludingTheNullPlayer() {
        UUID player = UUID.fromString("11111111-2222-3333-4444-555555555555");
        CPianoBlockPlayNotePacket packet = new CPianoBlockPlayNotePacket(
                player, POS, new Vec3(1.5D, -2.25D, 3.0D), 39, (byte) 80, false, true);
        assertEquals(packet, roundTrip(packet, CPianoBlockPlayNotePacket::encode, CPianoBlockPlayNotePacket::decode));

        // playerUUID 走的是“先写一个 isNull 标志”的可空编码
        CPianoBlockPlayNotePacket anonymous = new CPianoBlockPlayNotePacket(
                null, POS, Vec3.ZERO, 87, (byte) -1, true, false);
        CPianoBlockPlayNotePacket decoded = roundTrip(anonymous, CPianoBlockPlayNotePacket::encode, CPianoBlockPlayNotePacket::decode);
        assertNull(decoded.playerUUID());
        assertEquals(87, decoded.tone());
        assertEquals((byte) -1, decoded.volume());
        assertTrue(decoded.stop());
    }

    @Test
    public void pianoBlockPlayMultipleNotesRoundTrips() {
        UUID player = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        Vec3[] positions = {new Vec3(0.5D, 1.0D, 2.5D), new Vec3(-1.0D, 0.0D, 4.0D)};
        Note[] notes = {new Note((byte) 39, (byte) 100), new Note((byte) 43, (byte) 50).withLayer((byte) 2)};

        CPianoBlockPlayMultipleNotesPacket packet =
                new CPianoBlockPlayMultipleNotesPacket(player, POS, positions, notes);
        CPianoBlockPlayMultipleNotesPacket decoded =
                roundTrip(packet, CPianoBlockPlayMultipleNotesPacket::encode, CPianoBlockPlayMultipleNotesPacket::decode);

        assertEquals(player, decoded.playerUUID());
        assertEquals(POS, decoded.sourcePos());
        assertArrayEquals(positions, decoded.positions());
        assertEquals("每个音符固定 3 字节，解码按 Note.FIELD_NUM 切片", notes.length, decoded.notes().length);
        for (int i = 0; i < notes.length; i++) {
            assertNoteEquals(notes[i], decoded.notes()[i]);
        }
    }

    @Test
    public void pianoBlockPlayMultipleNotesWithoutAnyNote() {
        CPianoBlockPlayMultipleNotesPacket packet =
                new CPianoBlockPlayMultipleNotesPacket(null, POS, new Vec3[0], new Note[0]);
        CPianoBlockPlayMultipleNotesPacket decoded =
                roundTrip(packet, CPianoBlockPlayMultipleNotesPacket::encode, CPianoBlockPlayMultipleNotesPacket::decode);

        assertNull(decoded.playerUUID());
        assertEquals(0, decoded.positions().length);
        assertEquals("空音符数据不能解码出负数个音符", 0, decoded.notes().length);
    }

    @Test
    public void pianoBlockPlayMultipleNotesKeepsPositionsAndNotesAligned() {
        // 处理方法会用 positions[i] 配 notes[i]，长度必须一一对应
        Vec3[] positions = {new Vec3(1.0D, 1.0D, 1.0D), new Vec3(2.0D, 2.0D, 2.0D), new Vec3(3.0D, 3.0D, 3.0D)};
        Note[] notes = {new Note((byte) 1, (byte) 10), new Note((byte) 2, (byte) 20), new Note((byte) 3, (byte) 30)};

        CPianoBlockPlayMultipleNotesPacket decoded = roundTrip(
                new CPianoBlockPlayMultipleNotesPacket(UUID.randomUUID(), POS, positions, notes),
                CPianoBlockPlayMultipleNotesPacket::encode,
                CPianoBlockPlayMultipleNotesPacket::decode);

        assertEquals(positions.length, decoded.positions().length);
        assertEquals(notes.length, decoded.notes().length);
        assertArrayEquals(positions, decoded.positions());
    }

    @Test
    public void multipleNotesSurviveAFullPianoChord() {
        // 88 键全按下的极端情况：编解码不能因为音符多而错位
        List<Vec3> positionList = new ArrayList<>();
        List<Note> noteList = new ArrayList<>();
        for (int tone = 0; tone < 88; tone++) {
            positionList.add(new Vec3(tone, 64.0D, -tone));
            noteList.add(new Note((byte) tone, (byte) 100).withLayer((byte) (tone % 3)));
        }
        Vec3[] positions = positionList.toArray(new Vec3[0]);
        Note[] notes = noteList.toArray(new Note[0]);

        CPianoBlockPlayMultipleNotesPacket decoded = roundTrip(
                new CPianoBlockPlayMultipleNotesPacket(UUID.randomUUID(), POS, positions, notes),
                CPianoBlockPlayMultipleNotesPacket::encode,
                CPianoBlockPlayMultipleNotesPacket::decode);

        assertEquals(88, decoded.notes().length);
        assertArrayEquals(positions, decoded.positions());
        for (int tone = 0; tone < 88; tone++) {
            assertNoteEquals(notes[tone], decoded.notes()[tone]);
        }
    }
}
