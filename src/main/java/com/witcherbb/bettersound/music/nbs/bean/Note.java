package com.witcherbb.bettersound.music.nbs.bean;

import com.witcherbb.bettersound.common.utils.Util;

import java.util.List;

public class Note {
    private byte pitch;
    private byte volume;
    private byte layer = 0;

    public Note(byte pitch, byte volume) {
        this.pitch = pitch;
        this.volume = volume;
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

    public Note withLayer(byte layer) {
        this.layer = layer;
        return this;
    }

    public void changeVolume(byte volume) {
        this.volume = volume;
    }

    public static byte[] getTones(List<Note> notes) {
        return Util.toArray(notes.stream().parallel().map(Note::getPitch).toList());
    }

    public static byte[] getVolumes(List<Note> notes) {
        return Util.toArray(notes.stream().parallel().map(Note::getVolume).toList());
    }

    // 实现编译码在网络中传递
    public byte[] encode() {
        return new byte[]{this.pitch, this.volume, this.layer};
    }

    public static Note decode(byte[] source) {
        return new Note(source[0], source[1]).withLayer(source[2]);
    }
}
