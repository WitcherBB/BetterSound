package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.blocks.entity.AbstractPianoBlockEntity;
import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.blocks.state.properties.PianoPart;
import com.witcherbb.bettersound.client.ModOptions;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.menu.inventory.AbstractPianoMenu;
import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import com.witcherbb.bettersound.music.nbs.bean.Note;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyPressedPacket;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyReleasedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractPianoScreen extends AbstractContainerScreen<AbstractPianoMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(BetterSound.MODID, "textures/gui/piano_block.png");
    private static final int textureWidth = 300;
    private static final int textureHeight = 300;
    private static final List<Integer> BLACKS;
    private static final List<Integer> KEYS_C = new ArrayList<>(List.of(3, 15, 27, 39, 51, 63, 75, 87));
    protected final List<PianoKeyButton> keys = new ArrayList<>();
    protected final List<PianoKeyButton> currentKeys = new ArrayList<>();

    protected int firstWhiteKey; // C4
    protected boolean clicked;

    protected final Level level;
    protected final AbstractPianoBlockEntity blockEntity;
    protected MinecraftExtender minecraftExtender;
    protected final ModOptions modOptions = ModOptions.getOptions();

    static {
        BLACKS = new ArrayList<>();
        BLACKS.add(1);
        int c = 3;
        for (int i = 0; i < 7; i++, c += 12) {
            int key = c + 1;
            BLACKS.addAll(List.of(key, key += 2, key += 3, key += 2, key + 2));
        }
    }

    public static List<Integer> blacks() {
        return BLACKS;
    }

    public AbstractPianoScreen(AbstractPianoMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.level = pMenu.getLevel();
        this.blockEntity = pMenu.getPianoBlockEntity();
        this.firstWhiteKey = this.blockEntity.getFirstWhiteKey();
        if (BLACKS.contains(this.firstWhiteKey)) {
            this.firstWhiteKey -= 1;
        }
    }

    public int getFirstWhiteKey() {
        return firstWhiteKey;
    }

    @Override
    protected void init() {
        this.minecraftExtender = ((MinecraftExtender) this.minecraft);
        this.imageWidth = 300;
        this.imageHeight = 145;
        super.init();

        this.initButton();
    }

    @Override
    public void resize(@NotNull Minecraft pMinecraft, int pWidth, int pHeight) {
        super.resize(pMinecraft, pWidth, pHeight);
        this.initButton();
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 0, 300, 145, textureWidth, textureHeight);
        pGuiGraphics.blit(TEXTURE, this.leftPos + 14, this.topPos + 31, 1, 0, 145, 272, 4, textureWidth, textureHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xe9e9e9, false);
    }

    private void initButton() {
        keys.clear();
        for (int i = 0; i < 88; i++) {
            if (BLACKS.contains(i))
                this.keys.add(new PianoKeyButton(KeyCategory.BLACK, i));
            else this.keys.add(new PianoKeyButton(KeyCategory.WHITE, i));
        }
        this.loadButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    private void loadButtons() {
        //移除琴键组件
        this.removeCurrentButtons();
        this.currentKeys.clear();
        //往currentKeys里添加琴键
        int whiteCount = 0;
        for (int i = 0; i < 88; i++) {
            boolean flag = BLACKS.contains(i);
            PianoKeyButton button = this.keys.get(i);
            if (i == firstWhiteKey - 1 && BLACKS.contains(i)) {
                currentKeys.add(button);
                button.update(-1, this.leftPos);
            } else if (i >= firstWhiteKey && whiteCount < 16) {
                currentKeys.add(button);
                button.update(whiteCount, this.leftPos);
                if (!flag) {
                    whiteCount++;
                }
            }
        }
        PianoKeyButton button1 = currentKeys.get(currentKeys.size() - 1);
        if (BLACKS.contains(button1.id + 1)) {
            keys.get(button1.id + 1).update(16, this.leftPos);
            currentKeys.add(keys.get(button1.id + 1));
        }
        //添加琴键到组件中
        List<PianoKeyButton> list = this.orderedButtons();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            PianoKeyButton button = list.get(i);
            this.addRenderableWidget(button);
        }
    }

    private List<PianoKeyButton> orderedButtons() {
        List<PianoKeyButton> lKeys = new ArrayList<>(this.currentKeys);
        int index = 0;
        int size = lKeys.size();
        for (int i = 0; i < size; i++) {
            PianoKeyButton key = lKeys.get(i);
            PianoKeyButton thatKey = lKeys.get(index);
            if (BLACKS.contains(key.id)) {
                if (BLACKS.contains(thatKey.id)) {
                    if (index < i)
                        i--;
                } else {
                    Collections.swap(lKeys, i, index);
                }
                index++;
            }
        }
        return lKeys;
    }

    private void removeCurrentButtons() {
        this.currentKeys.forEach(this::removeWidget);
    }

    /**
     *
     * @param delta 移动的白键数，正为左移，负为右移.
     */
    protected boolean buttonMove(double delta) {
        this.firstWhiteKey -= (int) delta;
        if (BLACKS.contains(this.firstWhiteKey))
            this.firstWhiteKey -= (int) delta;
        if (this.firstWhiteKey < 0) {
            this.firstWhiteKey = 0;
            return false;
        } else if (this.firstWhiteKey > 62) {
            this.firstWhiteKey = 62;
            return false;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.buttonMove(pDelta)) {
            this.loadButtons();
            this.mouseDragged(pMouseX, pMouseY, this.clicked ? 0 : 1, pDelta * PianoKeyButton.whiteWidth, 0);
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        int size = this.currentKeys.size();
        for (int i = 0; i < size; i++) {
            currentKeys.get(i).mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            this.clicked = true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            this.clicked = false;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public void onClose() {
        this.blockEntity.updateFirstWhiteKey(this);
        BlockPos pos = this.blockEntity.getBlockPos();
        BlockState state = this.level.getBlockState(pos);

        if (state.getBlock() instanceof PianoBlock pianoBlock) {
            Direction combinedDir = pianoBlock.getCombinedDirection(state.getValue(PianoBlock.PART), state.getValue(PianoBlock.FACING));
            BlockPos blockPos = pos.relative(combinedDir);

            while (level.getBlockEntity(blockPos) instanceof AbstractPianoBlockEntity pianoBlockEntity && (pianoBlockEntity.getFirstWhiteKey() != this.firstWhiteKey)) {
                BlockState state1 = level.getBlockState(blockPos);
                if (state1.getValue(PianoBlock.PART) == PianoPart.KEYBOARD_L || state1.getValue(PianoBlock.PART) == PianoPart.KEYBOARD_M || state1.getValue(PianoBlock.PART) == PianoPart.KEYBOARD_R) {
                    pianoBlockEntity.updateFirstWhiteKey(this);
                }
                combinedDir = pianoBlock.getCombinedDirection(state1.getValue(PianoBlock.PART), state1.getValue(PianoBlock.FACING));
                blockPos = blockPos.relative(combinedDir);
            }
        }
        super.onClose();
    }

    protected class PianoKeyButton extends AbstractButton {
        private static final byte NORM_VOLUME = (byte) 100;
        private final KeyCategory keyCategory;
        private final int id;
        private static final int blackWidth = 10;
        private static final int whiteWidth = 17;
        private final AbstractPianoScreen fatherInstance = AbstractPianoScreen.this;

        private final int zIndex;

        private boolean pressed = false;

        private static final String[] keyNames = new String[]{
                "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"
        };

        public PianoKeyButton(KeyCategory keyCategory, int id) {
            super(0, AbstractPianoScreen.this.topPos + 35, keyCategory == KeyCategory.WHITE ? 17 : 10, keyCategory == KeyCategory.WHITE ? 70 : 40, CommonComponents.GUI_DONE);
            this.keyCategory = keyCategory;
            this.id = id;
            if (keyCategory == KeyCategory.BLACK) zIndex = 3;
            else zIndex = 2;

            this.setMessage(getComponent(this.id));
            this.setTooltip(Tooltip.create(this.getMessage()));
        }

        @Override
        public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            int x = this.getX();
            int y = this.getY();

            if (this.pressed) {
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                pGuiGraphics.blit(TEXTURE, x, y, this.zIndex,
                        this.judgeCategory(44, 17),
                        149, this.width, this.height, textureWidth, textureHeight);
            } else {
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                pGuiGraphics.blit(TEXTURE, x, y, this.zIndex,
                        this.judgeCategory(34, 0),
                        149, this.width, this.height, textureWidth, textureHeight);
            }
        }

        private <T> T judgeCategory(T blackValue, T whiteValue) {
            return this.keyCategory == KeyCategory.BLACK ? blackValue : whiteValue;
        }

        @Override
        public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }

        @Override
        public void onPress() {
            this.press();
        }

        @Override
        protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
            if (!this.pressed) {
                this.onPress();
            }
        }

        @Override
        public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
            boolean flag = this.clicked(pMouseX, pMouseY);
            if (this.active && this.visible) {
                if (this.isValidClickButton(pButton)) {
                    if (flag) {
                        this.onDrag(pMouseX, pMouseY, pDragX, pDragY);
                        return true;
                    } else if (this.pressed){
                        this.onRelease(pMouseX, pMouseY);
                    }
                }

            }
            return false;
        }

        @Override
        protected boolean clicked(double pMouseX, double pMouseY) {
            boolean flag = super.clicked(pMouseX, pMouseY);
            switch (this.blackAround()) {
                case 0b01:
                    if (!flag || pMouseX >= this.getX() + 12 && pMouseX < this.getX() + this.width && pMouseY < this.getY() + 40) {
                        return false;
                    }
                    break;
                case 0b10:
                    if (!flag || pMouseX >= this.getX() && pMouseX < this.getX() + 5 && pMouseY < this.getY() + 40) {
                        return false;
                    }
                    break;
                case 0b11:
                    if (!flag || ((pMouseX >= this.getX() + 12 && pMouseX < this.getX() + this.width) || (pMouseX >= this.getX() && pMouseX < this.getX() + 5)) && pMouseY < this.getY() + 40) {
                        return false;
                    }
                    break;
            }
            return flag;
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (this.active && this.visible) {
                if (this.isValidClickButton(pButton)) {
                    boolean flag = this.clicked(pMouseX, pMouseY);
                    if (flag) {
                        this.onClick(pMouseX, pMouseY);
                        return true;
                    }
                }

            }
            return false;
        }

        @Override
        public void onRelease(double pMouseX, double pMouseY) {
            this.release();
        }

        public void press() {
            this.pressed = true;
            if (minecraft != null) {
                BlockPos pos = fatherInstance.blockEntity.getBlockPos();
                ModNetwork.sendToServer(new SPianoKeyPressedPacket(pos, this.id, NORM_VOLUME));
                minecraftExtender.betterSound$getmodSoundManager().playPianoSound(ModSoundEvents.pianoSounds.get(this.id).get(), minecraft.player.getUUID(), pos, this.id, Note.toPianoSoundVolume(NORM_VOLUME), true, false);
            }
        }

        public void release() {
            this.pressed = false;
            BlockPos pos = fatherInstance.blockEntity.getBlockPos();
            if (!fatherInstance.blockEntity.isSoundDelay())
                if (minecraft != null) {
                    minecraftExtender.betterSound$getmodSoundManager().tryToStopPianoSound(minecraft.player.getUUID(), pos, this.id);
                }
            ModNetwork.sendToServer(new SPianoKeyReleasedPacket(pos, this.id, !fatherInstance.blockEntity.isSoundDelay()));
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
            defaultButtonNarrationText(pNarrationElementOutput);
        }

        /**
         * @return <code>0</code> - 无黑键</br>
         *         <code>1</code> - 黑键在右侧</br>
         *         <code>2</code> - 黑键在左侧</br>
         *         <code>3</code> - 黑键在两侧</br>
         * */
        protected byte blackAround() {
            byte b = 0b00;
            if (!BLACKS.contains(this.id)) {
                if (BLACKS.contains(this.id - 1)) {
                    b |= 0b10;
                }
                if (BLACKS.contains(this.id + 1)) {
                    b |= 0b01;
                }
            }
            return b;
        }

        public void update(int position, int leftPos) {
            this.calX(position, leftPos);
        }

        private void calX(int position, int leftPos) {
            final int firstWhite = leftPos + 14;
            int x = firstWhite + whiteWidth * position;
            if (position == -1) {
                x = firstWhite;
                this.width = blackWidth / 2;
            } else if (position == 16 && BLACKS.contains(this.id)) {
                x -= blackWidth / 2;
                this.width = blackWidth / 2;
            } else if (BLACKS.contains(this.id)) {
                x -= 5;
                this.width = blackWidth;
            }
            this.setX(x);
        }

        private static Component getComponent(int id) {
            int length = keyNames.length;

            if (id < 0 || id >= 88) {
                return Component.literal("Invalid key ID");
            } else if (id < 3) {
                return switch (id) {
                    case 0 -> Component.literal("A0");
                    case 1 -> Component.literal("A#/Bb0");
                    case 2 -> Component.literal("B0");
                    default -> Component.empty();
                };
            }

            int effectiveId = (id - 3) % length;
            int depth = (id - 3) / length + 1;

            return Component.literal(keyNames[effectiveId] + depth);
        }
    }

    protected enum KeyCategory {
        WHITE,
        BLACK
    }
}
