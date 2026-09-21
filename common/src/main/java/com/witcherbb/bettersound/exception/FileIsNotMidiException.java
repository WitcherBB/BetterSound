package com.witcherbb.bettersound.exception;

import net.minecraft.network.chat.Component;

import java.io.File;

public class FileIsNotMidiException extends Exception {
    public FileIsNotMidiException(String message) {
        super(message);
    }

    public FileIsNotMidiException(File file) {
        this(Component.translatable("exception.bettersound.fileisnotmidi", file.getName()).getString());
    }
}
