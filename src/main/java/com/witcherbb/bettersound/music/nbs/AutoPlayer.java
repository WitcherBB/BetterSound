package com.witcherbb.bettersound.music.nbs;

import com.witcherbb.bettersound.music.midi.MidiPlayer;

public interface AutoPlayer {
    NBSPlayer getNBSPlayer();

    MidiPlayer getMidiPlayer();

    void playNBSOn();

    void stopNBS();

    void pauseNBS();

    void playMidiOn();

    void stopMidi();

    void pauseMidi();
}
