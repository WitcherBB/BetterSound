package com.witcherbb.bettersound.exception;

import net.minecraft.network.chat.Component;

public class MidiNotFoundException extends Exception {
    public MidiNotFoundException(String filename) {
        super(Component.translatable("exception.bettersound.midinotfound", filename).getString());
    }
}
