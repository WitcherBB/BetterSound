package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.NoteBlockEntity;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.SNoteBlockPlayNotePacket;
import com.witcherbb.bettersound.menu.inventory.NoteBlockMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NoteBlockScreen extends AbstractContainerScreen<NoteBlockMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(BetterSound.MODID, "textures/gui/note_block_keyboard.png");
	private static final List<Integer> BLACKS = List.of(0, 2, 4, 7, 9, 12, 14, 16, 19, 21, 24);
	private final List<PianoKeyButton> blackButtonList = new ArrayList<>();
	private final List<PianoKeyButton> whiteButtonList = new ArrayList<>();

	private final Level level;
	private final NoteBlockEntity blockEntity;

	public NoteBlockScreen(NoteBlockMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.level = pMenu.level();
		this.blockEntity = pMenu.getBlockEntity();
	}

	@Override
	protected void init() {
		this.imageWidth = 254;
		this.imageHeight = 145;
		super.init();

		for (int i = 0; i <= 24; i++) {
			boolean flag = BLACKS.contains(i);
			if (flag)
				blackButtonList.add(new PianoKeyButton(KeyCategory.BLACK, i));
			else
				whiteButtonList.add(new PianoKeyButton(KeyCategory.WHITE, i));
		}
		this.addKeys();
	}

	@Override
	public void resize(@NotNull Minecraft pMinecraft, int pWidth, int pHeight) {
		blackButtonList.clear();
		whiteButtonList.clear();
		super.resize(pMinecraft, pWidth, pHeight);
	}

	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);

		pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);
		pGuiGraphics.blit(TEXTURE, this.leftPos + 2, this.topPos + 31, 2, 216, 251, 4);
	}

	@Override
	public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		renderBackground(pGuiGraphics);
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
	}

	@Override
	protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
		pGuiGraphics.drawString(this.font, Component.translatable("block.bettersound.note_block.title"), this.titleLabelX, this.titleLabelY, 0xe9e9e9, true);
	}

	private void addKeys() {
		int sizeBlack = blackButtonList.size();
		for (int i = 0; i < sizeBlack; i++) {
			this.addRenderableWidget(blackButtonList.get(i));
		}
		int sizeWhite = whiteButtonList.size();
		for (int i = 0; i < sizeWhite; i++) {
			this.addRenderableWidget(whiteButtonList.get(i));
		}
	}

	protected class PianoKeyButton extends AbstractButton {
		private final KeyCategory keyCategory;
		private final int id;
		private final int blackWidth = 10;
		private final int whiteWidth = 17;
		private final NoteBlockScreen fatherInstance = NoteBlockScreen.this;

		private static final String[] keyNames = new String[]{
				"F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B", "C", "C#/Db", "D", "D#/Eb", "E", "F"
		};

		public PianoKeyButton(KeyCategory keyCategory, int id) {
			super(0, NoteBlockScreen.this.topPos + 35, keyCategory == KeyCategory.WHITE ? 17 : 10, keyCategory == KeyCategory.WHITE ? 70 : 40, CommonComponents.GUI_DONE);
			this.keyCategory = keyCategory;
			this.id = id;

			this.calX();
			this.setTooltip(Tooltip.create(getComponent()));
			this.setTabOrderGroup(judgeCategory(2, 1));
		}

		@Override
		public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
			super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		}

		@Override
		protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
			int x = this.getX();
			int y = this.getY();
			BlockState state = level.getBlockState(blockEntity.getBlockPos());

			if (state.getBlock() instanceof NoteBlock) {
				if (this.id == state.getValue(NoteBlock.NOTE)) {
					RenderSystem.enableBlend();
					RenderSystem.enableDepthTest();
					pGuiGraphics.blit(TEXTURE, x, y, this.judgeCategory(200, 100),
							this.judgeCategory(44, 17),
							145,
							this.judgeCategory(10, 17),
							this.judgeCategory(40, 70), 255, 255);
				} else {
					RenderSystem.enableBlend();
					RenderSystem.enableDepthTest();
					pGuiGraphics.blit(TEXTURE, x, y, this.judgeCategory(200, 100),
							this.judgeCategory(34, 0),
							145,
							this.judgeCategory(10, 17),
							this.judgeCategory(40, 70), 255, 255);
				}
			}
		}

		private <T> T judgeCategory(T blackValue, T whiteValue) {
			return this.keyCategory == KeyCategory.BLACK ? blackValue : whiteValue;
		}

		@Override
		public void onPress() {
			BlockPos pos = NoteBlockScreen.this.blockEntity.getBlockPos();
			ModNetwork.sendToServer(new SNoteBlockPlayNotePacket(this.id, pos));
		}

		@Override
		public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
			if (this.isActive()) {
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
		public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
			return super.mouseReleased(pMouseX, pMouseY, pButton);
		}

		@Override
		protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

		}

		private void calX() {
			final int firstWhite = 8;
			final int firstBlack = 3;

			this.setX(fatherInstance.leftPos + switch (id) {
				case 0 -> firstBlack;
				case 1 -> firstWhite;
				case 2 -> firstBlack + whiteWidth;
				case 3 -> firstWhite + whiteWidth;
				case 4 -> firstBlack + 2 * whiteWidth;
				case 5 -> firstWhite + 2 * whiteWidth;
				case 6 -> firstWhite + 3 * whiteWidth;
				case 7 -> firstBlack + 4 * whiteWidth;
				case 8 -> firstWhite + 4 * whiteWidth;
				case 9 -> firstBlack + 5 * whiteWidth;
				case 10 -> firstWhite + 5 * whiteWidth;
				case 11 -> firstWhite + 6 * whiteWidth;
				case 12 -> firstBlack + 7 * whiteWidth;
				case 13 -> firstWhite + 7 * whiteWidth;
				case 14 -> firstBlack + 8 * whiteWidth;
				case 15 -> firstWhite + 8 * whiteWidth;
				case 16 -> firstBlack + 9 * whiteWidth;
				case 17 -> firstWhite + 9 * whiteWidth;
				case 18 -> firstWhite + 10 * whiteWidth;
				case 19 -> firstBlack + 11 * whiteWidth;
				case 20 -> firstWhite + 11 * whiteWidth;
				case 21 -> firstBlack + 12 * whiteWidth;
				case 22 -> firstWhite + 12 * whiteWidth;
				case 23 -> firstWhite + 13 * whiteWidth;
				case 24 -> firstBlack + 14 * whiteWidth;
                default -> throw new IllegalStateException("Unexpected value: " + id);
            });
		}

		private Component getComponent() {
			return Component.nullToEmpty(keyNames[getFitIndex(this.id)]);
		}

		private static int getFitIndex(int id) {
            int length = keyNames.length;
			if (id >= length) {
				return getFitIndex(id - length);
			} else {
				return id;
			}
		}
	}

	protected enum KeyCategory {
		WHITE,
		BLACK
	}
}
