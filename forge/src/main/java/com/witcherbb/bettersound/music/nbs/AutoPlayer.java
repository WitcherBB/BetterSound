package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.music.AutoMusicPlayer;
import com.witcherbb.bettersound.music.midi.MidiPlayer;

/**
 * 能自动演奏的方块。
 *
 * <p>演奏状态只有一份，存在共用的 {@link AutoMusicPlayer} 里；{@link NBSPlayer} 与
 * {@link MidiPlayer} 只是两种曲目各自的投递入口（各自负责「曲目从哪来、怎么发给服务端、
 * 延音怎么释放」）。因此下面这些传输控制方法对两者是同一件事，NBS 与 MIDI 的实现完全一致。
 */
public interface AutoPlayer {
    /** 共用的自动演奏引擎：传输控制与播放状态都问它 */
    AutoMusicPlayer getMusicPlayer();

    /** NBS 曲目入口 */
    NBSPlayer getNBSPlayer();

    /** MIDI 曲目入口 */
    MidiPlayer getMidiPlayer();

    void playNBSOn();

    void stopNBS();

    void pauseNBS();

    void playMidiOn();

    void stopMidi();

    void pauseMidi();
}
