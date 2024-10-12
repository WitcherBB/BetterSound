package com.witcherbb.bettersound.exception;

import net.minecraft.network.chat.Component;

import java.io.File;

public class FileIsNotNBSException extends Exception {
    public FileIsNotNBSException(String message) {
        super(message);
    }

    public FileIsNotNBSException(File file) {
        this(Component.translatable("exception.bettersound.fileisnotnbs", file.getName()).getString());
    }
}
