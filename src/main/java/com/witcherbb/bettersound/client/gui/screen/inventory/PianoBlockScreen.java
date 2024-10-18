package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.client.gui.screen.controls.PianoKeyBindsScreen;
import com.witcherbb.bettersound.menu.inventory.AbstractPianoMenu;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.SBlockEntityDataChangePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import java.awt.*;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class PianoBlockScreen extends AbstractPianoScreen {
    protected static final Component KEY_CONTROLL = Component.translatable("block.bettersound.piano.use_keymap").withStyle(Style.EMPTY.withFont(new ResourceLocation(BetterSound.MODID, "fzjz")));
    private final boolean[] pressedStates = new boolean[88];
    private boolean pedalPressed;
    private Button keybindsButton;
    private Checkbox keyCtrledCheckbox;

    public PianoBlockScreen(AbstractPianoMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.keybindsButton = this.addRenderableWidget(Button.builder(Component.translatable("controls.keybinds"), pButton -> {
            this.minecraft.setScreen(new PianoKeyBindsScreen(this));
        }).bounds(this.leftPos + 50, this.topPos + 115, 60, 20).build());
        int textWidth = this.font.width(KEY_CONTROLL);
        this.keyCtrledCheckbox = this.addRenderableWidget(new Checkbox(this.leftPos + this.imageWidth - textWidth - 24 - 50, this.topPos + 115, 20, 20,
                KEY_CONTROLL, false));
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.keyCtrledCheckbox.selected()) {
            if (modOptions.keyPianoSustainPedal.get().isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
                if (!this.pedalPressed) this.pressPedal(true);
                return true;
            }
            Set<Map.Entry<Lazy<KeyMapping>, Integer>> entrySet = modOptions.keys.entrySet();
            for (Map.Entry<Lazy<KeyMapping>, Integer> entry : entrySet) {
                int keyValue = entry.getValue();
                if (entry.getKey().get().isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
                    if (!this.pressedStates[keyValue]) {
                        this.keys.get(keyValue).press();
                        this.pressedStates[keyValue] = true;
                    }
                    return true;
                }
            }
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.keyCtrledCheckbox.selected()) {
            if (modOptions.keyPianoSustainPedal.get().isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
                if (this.pedalPressed) this.pressPedal(false);
                return true;
            }
            Set<Map.Entry<Lazy<KeyMapping>, Integer>> entrySet = modOptions.keys.entrySet();
            for (Map.Entry<Lazy<KeyMapping>, Integer> entry : entrySet) {
                int keyValue = entry.getValue();
                if (entry.getKey().get().isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
                    if (this.pressedStates[keyValue]) {
                        this.keys.get(entry.getValue()).release();
                        pressedStates[keyValue] = false;
                    }
                    return true;
                }
            }
        }

        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    private void pressPedal(boolean isPressed) {
        this.pedalPressed = isPressed;
        ModNetwork.sendToServer(new SBlockEntityDataChangePacket(blockEntity.getBlockPos(), isPressed));
    }
}
