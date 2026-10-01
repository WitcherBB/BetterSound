package com.witcherbb.bettersound.client.gui.screen.inventory;

import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputConstants;
import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.client.gui.screen.controls.PianoKeyBindsScreen;
import com.witcherbb.bettersound.common.platform.client.ClientPlatform;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.SBlockEntityDataChangePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.Set;

//TODO 添加搜索音轨文件界面，直接通过UI界面导入nbs或者midi文件

public class PianoBlockScreen extends AbstractPianoScreen {
    protected static final Component KEY_CONTROLL = Component.translatable("block.bettersound.piano.use_keymap").withStyle(Style.EMPTY.withFont(new ResourceLocation(Constants.MOD_ID, "fzjz")));
    private final boolean[] pressedStates = new boolean[88];
    private boolean pedalPressed;
    private Button keybindsButton;
    private Checkbox keyCtrledCheckbox;

    public PianoBlockScreen(BlockPos pos) {
        super(pos);
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
            if (ClientPlatform.hooks().isActiveAndMatchesKey(modOptions.getKeyPianoSustainPedal().get(), InputConstants.getKey(pKeyCode, pScanCode))) {
                if (!this.pedalPressed) this.pressPedal(true);
                return true;
            }
            Set<Map.Entry<Supplier<KeyMapping>, Integer>> entrySet = modOptions.getPianokeys().entrySet();
            for (Map.Entry<Supplier<KeyMapping>, Integer> entry : entrySet) {
                int keyValue = entry.getValue();
                if (ClientPlatform.hooks().isActiveAndMatchesKey(entry.getKey().get(), InputConstants.getKey(pKeyCode, pScanCode))) {
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
            if (ClientPlatform.hooks().isActiveAndMatchesKey(modOptions.getKeyPianoSustainPedal().get(), InputConstants.getKey(pKeyCode, pScanCode))) {
                if (this.pedalPressed) this.pressPedal(false);
                return true;
            }
            Set<Map.Entry<Supplier<KeyMapping>, Integer>> entrySet = modOptions.getPianokeys().entrySet();
            for (Map.Entry<Supplier<KeyMapping>, Integer> entry : entrySet) {
                int keyValue = entry.getValue();
                if (ClientPlatform.hooks().isActiveAndMatchesKey(entry.getKey().get(), InputConstants.getKey(pKeyCode, pScanCode))) {
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
        if (this.minecraft.level.getBlockEntity(this.position) instanceof PianoBlockEntity blockEntity){
            ModNetwork.sendToServer(new SBlockEntityDataChangePacket(blockEntity.getBlockPos(), isPressed));
        }
    }
}
