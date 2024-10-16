package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import com.witcherbb.bettersound.menu.inventory.AbstractPianoMenu;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.SBlockEntityDataChangePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class PianoBlockScreen extends AbstractPianoScreen {
    private final boolean[] pressedStates = new boolean[88];
    private boolean pedalPressed;

    public PianoBlockScreen(AbstractPianoMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (modOptions.KEY_PIANO_SUSTAIN_PEDAL.get().isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
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

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (modOptions.KEY_PIANO_SUSTAIN_PEDAL.get().isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
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

        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    private void pressPedal(boolean isPressed) {
        this.pedalPressed = isPressed;
        ModNetwork.sendToServer(new SBlockEntityDataChangePacket(blockEntity.getBlockPos(), isPressed));
    }
}
