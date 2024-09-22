package com.witcherbb.bettersound.client.gui.screen.controls;

import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PianoKeyBindSScreen extends OptionsSubScreen {
    public PianoKeyBindSScreen(Screen pLastScreen, Options pOptions, Component pTitle) {
        super(pLastScreen, pOptions, pTitle);
    }
}
