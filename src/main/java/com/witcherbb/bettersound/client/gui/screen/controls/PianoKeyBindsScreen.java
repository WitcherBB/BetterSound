package com.witcherbb.bettersound.client.gui.screen.controls;

import com.witcherbb.bettersound.client.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PianoKeyBindsScreen extends OptionsSubScreen {
    protected final ModOptions modOptions = ModOptions.getOptions();

    public PianoKeyBindsScreen(Screen pLastScreen) {
        super(pLastScreen, Minecraft.getInstance().options, Component.translatable("controls.keybinds.title"));
    }

    @Override
    public void removed() {
        this.modOptions.save();
    }
}
