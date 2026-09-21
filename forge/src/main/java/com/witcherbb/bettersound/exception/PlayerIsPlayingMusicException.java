package com.witcherbb.bettersound.exception;

import net.minecraft.network.chat.Component;

public class PlayerIsPlayingMusicException extends Exception {
    public PlayerIsPlayingMusicException() {
        super(Component.translatable("wrong.bettersound.nbs.isplaying").getString());
    }
}
