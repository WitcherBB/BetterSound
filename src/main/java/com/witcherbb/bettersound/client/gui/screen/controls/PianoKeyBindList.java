package com.witcherbb.bettersound.client.gui.screen.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PianoKeyBindList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
    final PianoKeyBindsScreen keyBindsScreen;

    public PianoKeyBindList(PianoKeyBindsScreen keyBindsScreen, Minecraft minecraft) {
        super(minecraft, 0, 0, 0, 0, 0);
        this.keyBindsScreen = keyBindsScreen;
    }

}
