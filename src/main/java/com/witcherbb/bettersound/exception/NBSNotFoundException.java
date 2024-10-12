package com.witcherbb.bettersound.exception;

import net.minecraft.network.chat.Component;

public class NBSNotFoundException extends Exception {
    public NBSNotFoundException(String filename) {
        super(Component.translatable("exception.bettersound.nbsnotfound", filename).getString());
    }
}
