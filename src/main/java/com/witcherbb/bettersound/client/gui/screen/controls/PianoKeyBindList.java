package com.witcherbb.bettersound.client.gui.screen.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.ComponentModifier;
import com.witcherbb.bettersound.client.ModOptions;
import com.witcherbb.bettersound.client.gui.PianoUtil;
import com.witcherbb.bettersound.mixins.extenders.AbstractWidgetExtender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class PianoKeyBindList extends ContainerObjectSelectionList<PianoKeyBindList.Entry> {
    static final Minecraft mc = Minecraft.getInstance();
    static final ModOptions modOptions = ModOptions.getOptions();
    protected final int blackWidth = 10;
    protected final int blackHeight = 40;
    protected final int whiteWidth = 17;
    protected final int whiteHeight = 70;
    protected final int pedalWidth;
    protected final int pedalHeight;
    protected final int pedalLeftOffset;
    protected final int pedalTopOffset;
    protected final int rowLeftMin;
    protected final PianoKeyBindsScreen keyBindsScreen;
    protected final List<PianoKeyEntry> keyEntries = new ArrayList<>();
    protected int whiteCountInRow;
    protected int keyboardWidth;

    public PianoKeyBindList(PianoKeyBindsScreen keyBindsScreen, Minecraft minecraft) {
        super(minecraft, keyBindsScreen.width, keyBindsScreen.height, 0, keyBindsScreen.height, 70);
        this.keyBindsScreen = keyBindsScreen;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);

        this.whiteCountInRow = 26;
        this.pedalHeight = 20;
        this.pedalWidth = 100;
        this.rowLeftMin = (keyBindsScreen.width - whiteWidth * whiteCountInRow) / 2;
        this.pedalLeftOffset = this.rowLeftMin + (whiteWidth * whiteCountInRow - pedalWidth) / 2;
        this.pedalTopOffset = 300;
        this.keyboardWidth = whiteCountInRow * whiteWidth;

        Map<Lazy<KeyMapping>, Integer> pianokeys = modOptions.getPianokeys();
        pianokeys.forEach(((keyMappingLazy, id) -> {
            PianoKeyEntry entry = new PianoKeyEntry(keyMappingLazy.get(), id);
            if (entry.isBlack) this.addKey(entry);
            else this.addKeyToTop(entry);
            this.addEntry(entry);
        }));

        KeyMapping pedalKey = modOptions.getKeyPianoSustainPedal().get();
        PedalButton pedalButton = new PedalButton(0, 0, pedalWidth, pedalHeight, Component.empty());
        this.addEntry(new Entry(pedalKey, pedalButton) {
            @Override
            public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
                this.keyButton.setPosition(pLeft, pTop);
                this.keyButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
                Component label = Component.translatable(pedalKey.getName());
                pGuiGraphics.drawCenteredString(mc.font, label, this.keyButton.getX() + this.keyButton.getWidth() / 2, this.keyButton.getY() - mc.font.lineHeight - 5, 0xD0D0D0);
            }

            @Override
            public void reload() {
                super.reload();
                ((PedalButton) this.keyButton).setTextColor(this.textColor);
            }
        });
    }

    public void reloadEntries() {
        KeyMapping.resetMapping();
        int size = this.children().size();
        for (int i = 0; i < size; i++) {
            this.children().get(i).reload();
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.isMouseOver(pMouseX, pMouseY)) {
            Entry entry = this.getEntryAtPos(pMouseX, pMouseY);
            if (entry != null) {
                if (entry.mouseClicked(pMouseX, pMouseY, pButton)) {
                    Entry focused = this.getFocused();
                    if (focused != null && focused != entry) {
                        ((ContainerEventHandler) focused).setFocused(null);
                    }

                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (pButton == 0) {
                this.setFocused(null);
                this.setDragging(false);
                this.clickedHeader((int) (pMouseX - (double) (this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int) (pMouseY - (double) this.y0) + (int) this.getScrollAmount() - 4);
                return true;
            }
            this.setFocused(null);
            this.setDragging(false);
        }
        return false;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener pListener) {
        super.setFocused(pListener);
        if (pListener == null) {
            for (Entry entry : children()) {
                entry.keyButton.setFocused(false);
            }
        }
    }

    @Override
    protected void renderList(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int size = this.getItemCount();

        for (int i = 0; i < size; i++) {
            Entry entry = this.getEntry(i);
            int index = i;
            if (entry instanceof PianoKeyEntry) {
                PianoKeyEntry keyEntry = this.getKey(i);
                index = keyEntry.id;
                int x = this.getRowLeft(index);
                int y = this.getRowTop(index);
                int width = PianoUtil.isBlackey(i) ? blackWidth : whiteWidth;
                int height = PianoUtil.isBlackey(i) ? blackHeight : whiteHeight;
                this.renderItem(pGuiGraphics, pMouseX, pMouseY, pPartialTick, index, x, y, width, height);
            } else {
                this.renderItem(pGuiGraphics, pMouseX, pMouseY, pPartialTick, index, pedalLeftOffset, pedalTopOffset, pedalWidth, pedalHeight);
            }
        }
    }

    @Override
    protected void renderItem(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, int pIndex, int pLeft, int pTop, int pWidth, int pHeight) {
        super.renderItem(pGuiGraphics, pMouseX, pMouseY, pPartialTick, pIndex, pLeft, pTop, pWidth, pHeight);
    }

    @Override
    protected int getRowTop(int pIndex) {
        return pIndex < 44 ? 100 : 200;
    }

    protected Entry getEntryAtPos(double pMouseX, double pMouseY) {
        if (this.inKeboardRange(pMouseX, pMouseY)) {
            int preKey = 0;
            if (pMouseY >= 200 && pMouseY < 200 + whiteHeight) preKey = whiteCountInRow;
            int whiteOffset = Mth.floor((pMouseX - rowLeftMin) / whiteWidth) + preKey;
            int offset = whiteOffset + this.preBlackCount(whiteOffset, CountingPrin.WHITE);
            Entry entry = this.getEntry(offset);
            int buttonX = entry.keyButton.getX();
            int buttonY = entry.keyButton.getY();
            int leftOffset = pMouseX >= buttonX && pMouseX < buttonX + (double) blackWidth / 2 && pMouseY < buttonY + blackHeight ? -1 : 0;
            int rightOffset = pMouseX >= buttonX + whiteWidth - (double) blackWidth / 2 && pMouseX < buttonX + whiteWidth && pMouseY < buttonY + blackHeight ? 1 : 0;
            offset += switch (blackAround(offset)) {
                case 0b01 -> rightOffset;
                case 0b10 -> leftOffset;
                case 0b11 -> leftOffset + rightOffset;
                default -> 0;
            };
            return this.getEntry(offset);
        } else if ((pMouseY >= pedalTopOffset && pMouseY < pedalTopOffset + pedalHeight) && (pMouseX >= pedalLeftOffset && pMouseX < pedalLeftOffset + pedalWidth)) {
            return this.getEntry(this.getItemCount() - 1);
        }
        return null;
    }

    protected int getRowLeft(int pIndex) {
        Entry entry = getEntry(pIndex);
        boolean flag = PianoUtil.isBlackey(pIndex);
        int offset = this.preBlackCount(pIndex, CountingPrin.COMMON) - (pIndex < 44 ? 0 : this.preBlackCount(44, CountingPrin.COMMON));
        pIndex = pIndex < 44 ? pIndex : pIndex - 44;

        if (entry instanceof PianoKeyEntry) {
            int x = rowLeftMin + whiteWidth * (pIndex - offset);
            if (flag) {
                x -= 5;
            }
            return x;
        } else {
            return rowLeftMin + 80;
        }
    }

    @Override
    public int getRowLeft() {
        return rowLeftMin;
    }

    @Override
    public int getRowRight() {
        return this.getRowLeft() + keyboardWidth;
    }

    private void addKey(PianoKeyEntry entry) {
        this.keyEntries.add(entry);
    }

    private void addKeyToTop(PianoKeyEntry entry) {
        this.keyEntries.add(0, entry);
    }

    private PianoKeyEntry getKey(int pIndex) {
        return this.keyEntries.get(pIndex);
    }

    private boolean inKeboardRange(double pMouseX, double pMouseY) {
        return ((pMouseY >= 100 && pMouseY < 100 + whiteHeight) || (pMouseY >= 200 && pMouseY < 200 + whiteHeight))
                && (pMouseX >= rowLeftMin && pMouseX < rowLeftMin + keyboardWidth);
    }

    private int preBlackCount(int index, CountingPrin principle) {
        int count = 0;
        int group = principle == CountingPrin.COMMON ? 12 : 7;
        int firstKeys = principle == CountingPrin.COMMON ? 3 : 2;
        if (index == firstKeys - 1) count = 1;
        else if (index >= firstKeys) {
            count = 1;
            int groupIndex = (index - firstKeys) / group;
            int offset = 0;
            if (principle == CountingPrin.COMMON) {
                offset = switch ((index - firstKeys) % group) {
                    case 0, 1 -> 0;
                    case 2, 3 -> 1;
                    case 4, 5, 6 -> 2;
                    case 7, 8 -> 3;
                    case 9, 10 -> 4;
                    case 11 -> 5;
                    default -> 6;
                };
            } else if (principle == CountingPrin.WHITE) {
                offset = switch ((index - firstKeys) % group) {
                    case 0 -> 0;
                    case 1 -> 1;
                    case 2, 3 -> 2;
                    case 4 -> 3;
                    case 5 -> 4;
                    case 6 -> 5;
                    default -> 6;
                };
            }
            count += groupIndex * 5 + offset;
        }

        return count;
    }

    protected static byte blackAround(int index) {
        byte b = 0b00;
        if (!PianoUtil.isBlackey(index)) {
            if (PianoUtil.isBlackey(index - 1)) {
                b |= 0b10;
            }
            if (PianoUtil.isBlackey(index + 1)) {
                b |= 0b01;
            }
        }
        return b;
    }

    // 不渲染滚动条
    @Override
    public int getMaxScroll() {
        return 0;
    }

    // 不滚动
    @Override
    protected void updateScrollingState(double pMouseX, double pMouseY, int pButton) {
    }

    /* *************************************************** */
    @OnlyIn(Dist.CLIENT)
    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        protected static final Minecraft mc = Minecraft.getInstance();
        protected final KeyMapping key;
        protected Component keyname = Component.empty();
        protected AbstractButton keyButton;
        protected int textColor = 0xFFFFFF;
        protected boolean hasCollision;

        Entry(KeyMapping key, AbstractButton button) {
            this.keyButton = button;
            this.key = key;
            this.reload();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(keyButton);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            this.keyButton.setPosition(pLeft, pTop);
            this.keyButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            Vector2i pos = this.getKeyNamePos(this.keyButton);
            pGuiGraphics.drawString(mc.font, keyname, pos.x, pos.y, textColor, false);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(keyButton);
        }

        protected Vector2i getKeyNamePos(AbstractWidget widget) {
            return new Vector2i(widget.getX() + widget.getWidth() / 2 - mc.font.width(keyname) / 2, widget.getY() + keyButton.getHeight() / 2 - mc.font.lineHeight / 2);
        }

        public void reload() {
            this.keyButton.setMessage(ComponentModifier.start(this.key.getTranslatedKeyMessage())
                    .withFont("bettersound:fzjz").end());

            this.textColor = 0x00D50E;
            if (!this.key.isUnbound()) {
                for (Lazy<KeyMapping> keyMappingLazy : modOptions.keymappings) {
                    if (this.key != keyMappingLazy.get() && this.key.same(keyMappingLazy.get()) || this.key.hasKeyModifierConflict(keyMappingLazy.get())) {
                        this.hasCollision = true;
                        this.textColor = 0xFF5F5F;
                    }
                }
            }

            if (PianoKeyBindList.this.keyBindsScreen.selected != null && PianoKeyBindList.this.keyBindsScreen.selected == this.key) {
                this.keyButton.setMessage(ComponentModifier.start(Component.literal("> <"))
                        .withFont("bettersound:fzjz").end());
            }
        }
    }
    /* **************************************************** */
    @OnlyIn(Dist.CLIENT)
    public class PianoKeyEntry extends Entry {
        private final int id;
        private final boolean isBlack;


        PianoKeyEntry(KeyMapping pKey, int pId) {
            super(pKey, Button.builder(PianoUtil.getKeyName(pId), pButton -> {
                PianoKeyBindList.this.keyBindsScreen.selected = pKey;
                pKey.setKeyModifierAndCode(null, InputConstants.UNKNOWN);
                PianoKeyBindList.this.reloadEntries();
            }).bounds(0, 0, PianoUtil.isBlackey(pId) ? blackWidth : whiteWidth, PianoUtil.isBlackey(pId) ? blackHeight : whiteHeight).build(builder ->
                    new KeyButton(builder, pId)));
            this.id = pId;
            this.isBlack = PianoUtil.isBlackey(pId);
            this.keyButton.setTooltip(Tooltip.create(this.keyButton.getMessage()));
        }

        @Override
        protected Vector2i getKeyNamePos(AbstractWidget widget) {
            return new Vector2i(widget.getX() + widget.getWidth() / 2 - mc.font.width(keyname) / 2, widget.getY() + keyButton.getHeight() * 3 / 4 - mc.font.lineHeight / 2);
        }

        @Override
        public void reload() {
            this.keyname = ComponentModifier.start(this.key.getTranslatedKeyMessage())
                    .withFont("bettersound:fzjz").end();

            this.textColor = 0x00D50E;
            if (!this.key.isUnbound()) {
                for (Lazy<KeyMapping> keyMappingLazy : modOptions.keymappings) {
                    if (this.key != keyMappingLazy.get() && this.key.same(keyMappingLazy.get()) || this.key.hasKeyModifierConflict(keyMappingLazy.get())) {
                        this.hasCollision = true;
                        this.textColor = 0xFF5F5F;
                    }
                }
            }

            if (PianoKeyBindList.this.keyBindsScreen.selected != null && PianoKeyBindList.this.keyBindsScreen.selected == this.key) {
                this.keyname = ComponentModifier.start(Component.literal("> <"))
                        .withFont("bettersound:fzjz").end();
            }

            if (PianoKeyBindList.this.keyBindsScreen.selected == null && this.key.isUnbound()
                || PianoKeyBindList.this.keyBindsScreen.selected != null && this.key.isUnbound() && PianoKeyBindList.this.keyBindsScreen.selected != this.key) {
                this.keyname = Component.empty();
                this.keyButton.setFocused(false);
            }

        }
    }
    /* **************************************************** */
    @OnlyIn(Dist.CLIENT)
    public class KeyButton extends Button implements AbstractWidgetExtender {
        protected static final ResourceLocation TEXTURE = new ResourceLocation(BetterSound.MODID, "textures/gui/piano_keyboard.png");
        protected static final int textureWidth = 300;
        protected static final int textureHeight = 300;
        private final boolean isBlack;
        private final int id;

        protected KeyButton(Builder builder, int id) {
            super(builder);
            this.id = id;
            this.isBlack = PianoUtil.isBlackey(id);
        }

        @Override
        protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            int x = this.getX();
            int y = this.getY();
            int offsetX = this.isFocused() ? (isBlack ? 10 : 17) : 0;

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            pGuiGraphics.blit(TEXTURE, x, y,
                    isBlack ? 34 + offsetX : offsetX,
                    149, this.width, this.height, textureWidth, textureHeight);
        }

        @Override
        protected boolean clicked(double pMouseX, double pMouseY) {
            switch (blackAround(this.id)) {
                case 0b01:
                    if (pMouseX >= this.getX() + 12 && pMouseX < this.getX() + this.width && pMouseY < this.getY() + 40) {
                        return false;
                    }
                    break;
                case 0b10:
                    if (pMouseX >= this.getX() && pMouseX < this.getX() + 5 && pMouseY < this.getY() + 40) {
                        return false;
                    }
                    break;
                case 0b11:
                    if (((pMouseX >= this.getX() + 12 && pMouseX < this.getX() + this.width) || (pMouseX >= this.getX() && pMouseX < this.getX() + 5)) && pMouseY < this.getY() + 40) {
                        return false;
                    }
                    break;
            }
            return super.clicked(pMouseX, pMouseY);
        }

        @Override
        public boolean betterSound$ifOverridePreTip() {
            return true;
        }
    }

    public class PedalButton extends AbstractButton {
        private int textColor;

        public PedalButton(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
            super(pX, pY, pWidth, pHeight, pMessage);
        }

        @Override
        public void onPress() {
            KeyMapping pedalKey = modOptions.getKeyPianoSustainPedal().get();
            PianoKeyBindList.this.keyBindsScreen.selected = pedalKey;
            pedalKey.setKeyModifierAndCode(null, InputConstants.UNKNOWN);
            PianoKeyBindList.this.reloadEntries();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }

        @Override
        public void renderString(GuiGraphics pGuiGraphics, Font pFont, int pColor) {
            int x = this.getX() + (this.getWidth() - mc.font.width(this.getMessage())) / 2;
            int y = this.getY() + (this.getHeight() - mc.font.lineHeight) / 2;
            pGuiGraphics.drawString(mc.font, this.getMessage(), x, y, this.textColor, false);
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }
    }

    enum CountingPrin {
        COMMON, WHITE
    }
}
