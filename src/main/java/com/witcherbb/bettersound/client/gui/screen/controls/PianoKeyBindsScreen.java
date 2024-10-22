package com.witcherbb.bettersound.client.gui.screen.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.witcherbb.bettersound.client.ModOptions;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PianoKeyBindsScreen extends OptionsSubScreen {
    protected final ModOptions modOptions = ModOptions.getOptions();
    private PianoKeyBindList keyBindList;
    KeyMapping selected;

    public PianoKeyBindsScreen(Screen pLastScreen) {
        super(pLastScreen, Minecraft.getInstance().options, Component.translatable("controls.keybinds.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.keyBindList = this.addRenderableWidget(new PianoKeyBindList(this, Minecraft.getInstance()));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.selected != null) {
            modOptions.setKey(this.selected, InputConstants.UNKNOWN);
            this.selected = null;
            this.keyBindList.reloadEntries();
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.selected != null) {
            if (pKeyCode == 256) {
                this.selected.setKeyModifierAndCode(null, InputConstants.UNKNOWN);
                this.options.setKey(this.selected, InputConstants.UNKNOWN);
            } else {
                this.selected.setKeyModifierAndCode(null, InputConstants.getKey(pKeyCode, pScanCode));
                this.options.setKey(this.selected, InputConstants.getKey(pKeyCode, pScanCode));
            }

            if (pKeyCode == 256 || !net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(this.selected.getKey()))
                this.selected = null;
            this.keyBindList.reloadEntries();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        var key = InputConstants.getKey(pKeyCode, pScanCode);
        if (this.selected != null && this.selected.getKey() == key) {
            this.selected = null;
            this.keyBindList.reloadEntries();
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void removed() {
        this.modOptions.save();
    }
}
