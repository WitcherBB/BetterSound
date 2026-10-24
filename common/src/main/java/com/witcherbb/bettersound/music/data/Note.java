package com.witcherbb.bettersound.music.data;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * 音符流里的一个事件。NBS 与 MIDI 共用。
 *
 * <p>绝大多数是"演奏"（{@link #KIND_PLAY}）；此外还承载两类"释放延音"的哨兵事件：
 * <ul>
 *     <li>{@link #KIND_RELEASE}：松开某一个键（{@code pitch} 指出是哪个），用于逐音松键；</li>
 *     <li>{@link #KIND_RELEASE_ALL}：把这台琴上正在响的音整片收掉（安全阀、小节线兜底）；</li>
 *     <li>{@link #KIND_PEDAL_DOWN} / {@link #KIND_PEDAL_UP}：把踏板状态反映到方块上，
 *         让模型跟着曲子里的 CC64 变化，但<b>不切任何音</b>。</li>
 * </ul>
 * 把它们放进同一条「按刻索引」的音符流里之后，播放引擎就只剩"按刻派发"这一件事：
 * 释放点天然占据它自己的刻，引擎不需要再维护"待释放表"，也不需要每游戏刻扫描一遍。
 *
 * <p>⚠️ 哨兵只出现在「客户端 → 服务端」这一跳（{@code SMidiPlayPacket}）。服务端派发时会先把它们分流出去，
 * 转成既有的演奏/停止数据包，所以客户端的声音路径<b>永远看不到</b>释放事件——
 * 历史上 {@code tone = -1} 的哨兵正是漏进了声音路径，才导致 {@code pianoSounds.get(-1)} 越界崩溃。
 *
 * @see PianoSongTrack
 */
public class Note {
    /** 网络里每个事件定长占多少字节：pitch / volume / layer / duration（2 字节）/ kind */
    public static final int FIELD_NUM = 6;

    /** 正常演奏 */
    public static final byte KIND_PLAY = 0;
    /** 释放 pitch 这一个键 */
    public static final byte KIND_RELEASE = 1;
    /** 把这台琴上正在响的音整片收掉 */
    public static final byte KIND_RELEASE_ALL = 2;
    /** 踏板踩下：<b>只把状态反映到方块上（模型会跟着变），不切任何音</b> */
    public static final byte KIND_PEDAL_DOWN = 3;
    /** 踏板抬起：同上 */
    public static final byte KIND_PEDAL_UP = 4;

    /**
     * 没有时长信息时的取值。
     * <p>NBS 文件本身不记录音符时值；MIDI 里也可能有音符没有配对的 Note Off。
     * 这两种情况都落在同一个哨兵值上（与 {@code MidiSong.DURATION_UNKNOWN} 含义一致）。
     */
    public static final short DURATION_UNKNOWN = -1;

    private byte pitch;
    private byte volume;
    private byte layer = 0;
    /** 琴键按住多少个游戏刻；{@link #DURATION_UNKNOWN} 表示没有这份信息 */
    private short duration = DURATION_UNKNOWN;
    /** 事件种类，见 {@link #KIND_PLAY} 等常量 */
    private byte kind = KIND_PLAY;

    public Note(byte pitch, byte volume) {
        this.pitch = pitch;
        this.volume = volume;
    }

    /** 造一个"松开某个键"的释放事件 */
    public static Note release(byte tone) {
        return new Note(tone, (byte) 0).withKind(KIND_RELEASE);
    }

    /** 造一个"整片收掉"的释放事件 */
    public static Note releaseAll() {
        return new Note((byte) 0, (byte) 0).withKind(KIND_RELEASE_ALL);
    }

    /**
     * 造一个"踏板踩下"的事件。
     * <p>它只影响方块状态（也就是模型），不负责收任何音——收不收音由逐音释放事件决定。
     */
    public static Note pedalDown() {
        return new Note((byte) 0, (byte) 0).withKind(KIND_PEDAL_DOWN);
    }

    /** 造一个"踏板抬起"的事件，同样只影响方块状态 */
    public static Note pedalUp() {
        return new Note((byte) 0, (byte) 0).withKind(KIND_PEDAL_UP);
    }

    public static byte[] getTones(Note[] notes) {
        return ArrayUtils.toPrimitive(Arrays.stream(notes).parallel()
                .map(Note::getPitch)
                .toArray(Byte[]::new));
    }

    public static byte[] getVolumes(Note[] notes) {
        return ArrayUtils.toPrimitive(Arrays.stream(notes).parallel()
                .map(Note::getVolume)
                .toArray(Byte[]::new));
    }

    public static float toPianoSoundVolume(byte volume) {
        return volume * 3.0F / 100;
    }

    public byte getPitch() {
        return pitch;
    }

    public byte getLayer() {
        return layer;
    }

    public byte getVolume() {
        return volume;
    }

    /** 琴键按住多少个游戏刻；负数表示没有这份信息 */
    public short getDuration() {
        return duration;
    }

    /** 是否带着时长信息 */
    public boolean hasDuration() {
        return this.duration >= 0;
    }

    public byte getKind() {
        return kind;
    }

    /** 正常演奏（不是释放事件） */
    public boolean isPlay() {
        return this.kind == KIND_PLAY;
    }

    /** 释放 pitch 这一个键 */
    public boolean isReleaseTone() {
        return this.kind == KIND_RELEASE;
    }

    /** 整片收掉 */
    public boolean isReleaseAll() {
        return this.kind == KIND_RELEASE_ALL;
    }

    /** 是释放事件（整片或逐音） */
    public boolean isRelease() {
        return this.kind == KIND_RELEASE || this.kind == KIND_RELEASE_ALL;
    }

    /** 踏板踩下（只改外观，不切音） */
    public boolean isPedalDown() {
        return this.kind == KIND_PEDAL_DOWN;
    }

    /** 踏板抬起（只改外观，不切音） */
    public boolean isPedalUp() {
        return this.kind == KIND_PEDAL_UP;
    }

    /** 是踏板状态事件 */
    public boolean isPedal() {
        return this.kind == KIND_PEDAL_DOWN || this.kind == KIND_PEDAL_UP;
    }

    public Note withLayer(byte layer) {
        this.layer = layer;
        return this;
    }

    public Note withDuration(short duration) {
        this.duration = duration;
        return this;
    }

    public Note withKind(byte kind) {
        this.kind = kind;
        return this;
    }

    public void changeVolume(byte volume) {
        this.volume = volume;
    }

    // 实现编译码在网络中传递
    public byte[] encode() {
        return new byte[]{this.pitch, this.volume, this.layer,
                (byte) (this.duration >> 8), (byte) this.duration, this.kind};
    }

    /** @param source 长度必须是 {@link #FIELD_NUM}，见 {@code CPianoBlockPlayMultipleNotesPacket} 的定长切片 */
    public static Note decode(byte[] source) {
        return new Note(source[0], source[1])
                .withLayer(source[2])
                // 高字节带符号位，这样 DURATION_UNKNOWN(-1) 也能原样往返
                .withDuration((short) ((source[3] << 8) | (source[4] & 0xFF)))
                .withKind(source[5]);
    }
}
