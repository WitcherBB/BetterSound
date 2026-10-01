package com.witcherbb.bettersound.client.gui.screen.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.witcherbb.bettersound.client.ModOptions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PianoKeyBindsScreen extends Screen {
    protected final ModOptions modOptions = ModOptions.getOptions();
    private PianoKeyBindList keyBindList;
    KeyMapping selected;

    private Screen lastScreen;

    public PianoKeyBindsScreen(Screen pLastScreen) {
        super(Component.translatable("controls.keybinds.title"));
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
                // this.selected.setKeyModifierAndCode(null, InputConstants.UNKNOWN);
                this.selected.setKey(InputConstants.UNKNOWN);
            } else {
                // this.selected.setKeyModifierAndCode(null, InputConstants.getKey(pKeyCode, pScanCode));
                this.selected.setKey(InputConstants.getKey(pKeyCode, pScanCode));
            }

            modOptions.save();
            // if (pKeyCode == 256 || !KeyModifier.isKeyCodeModifier(this.selected.getKey()))
            this.selected = null;
            this.keyBindList.reloadEntries();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.selected != null && this.selected.matches(pKeyCode, pScanCode)) {
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
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void removed() {
        this.modOptions.save();
    }
}
