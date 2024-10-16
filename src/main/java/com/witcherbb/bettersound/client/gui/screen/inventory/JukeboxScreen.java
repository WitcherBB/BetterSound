package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.common.utils.Util;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.server.SJukeboxNamePacket;
import com.witcherbb.bettersound.menu.inventory.JukeboxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class JukeboxScreen extends AbstractContainerScreen<JukeboxMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(BetterSound.MODID, "textures/gui/jukebox.png");
	private final JukeboxBlockEntity blockEntity;
	private final Level level;
	private EditBox editBox;
	private ImageTip imageTip;
	private JukeboxConfirmButton confirmButton;

	public JukeboxScreen(JukeboxMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.blockEntity = pMenu.getBlockEntity();
		this.level = this.blockEntity.getLevel();
	}

	@Override
	protected void init() {
		super.init();
		this.editBox = new EditBox(this.font, this.leftPos + 52, this.topPos + 16, 64, 10, Component.translatable("block.bettersound.jukebox.name_input"));
		this.editBox.setMaxLength(50);
		this.editBox.setTextColor(0xFFFFFF);
		this.editBox.setCanLoseFocus(true);
		this.editBox.setMaxLength(30);
		this.editBox.setValue(blockEntity.getUpdateTag().getString("Name"));
		this.addWidget(this.editBox);
		this.imageTip = new ImageTip(this.leftPos + 112, this.topPos + 30, 14, 14);
		this.addRenderableWidget(this.imageTip);
		this.confirmButton = new JukeboxConfirmButton();
		this.addRenderableWidget(this.confirmButton);
	}

	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		String s = this.editBox.getValue();
		Util.Status status = this.imageTip.getStatus();
		this.init(pMinecraft, pWidth, pHeight);
		this.editBox.setValue(s);
		this.imageTip.updateStatus(status);
	}

	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);

		pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);
		this.editBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		renderBackground(pGuiGraphics);
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		renderTooltip(pGuiGraphics, pMouseX, pMouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
		super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
		pGuiGraphics.drawString(this.font, this.editBox.getMessage(), this.titleLabelX + 17, this.titleLabelY + 11, 4210752, false);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == 256 || !this.editBox.isFocused()) {
			return super.keyPressed(pKeyCode, pScanCode, pModifiers);
		} else {
			return this.editBox.keyPressed(pKeyCode, pScanCode, pModifiers);
		}
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		this.editBox.setFocused(false);
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	public JukeboxConfirmButton getConfirmButton() {
		return confirmButton;
	}

	public ImageTip getImageTip() {
		return imageTip;
	}

	public class JukeboxConfirmButton extends AbstractButton {

		public JukeboxConfirmButton() {
			super(JukeboxScreen.this.editBox.getX() + JukeboxScreen.this.editBox.getWidth() + 5, JukeboxScreen.this.editBox.getY(), 10, 10, CommonComponents.GUI_DONE);
		}

		@Override
		public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
			return super.mouseClicked(pMouseX, pMouseY, pButton);
		}

		@Override
		public void onPress() {
			String name = JukeboxScreen.this.editBox.getValue();
            if (Minecraft.getInstance().level != null) {
                ModNetwork.sendToServer(new SJukeboxNamePacket(name, Minecraft.getInstance().level.dimension().location().getPath()));
            }
        }

		@Override
		protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
			this.defaultButtonNarrationText(pNarrationElementOutput);
		}

		@Override
		protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
			int xOffset = 0;

			if (this.isHovered()) {
				xOffset = this.width;
			}

			pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), xOffset, 166, this.width, this.height);
			pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), 20, 166, this.width, this.height);
		}
	}

	public static class ImageTip extends AbstractWidget {
		private Util.Status status =Util.Status.NONE;

		public ImageTip(int pX, int pY, int pWidth, int pHeight) {
			super(pX, pY, pWidth, pHeight, CommonComponents.GUI_DONE);
		}

		@Override
		public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
			return false;
		}

		@Override
		protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
			this.defaultButtonNarrationText(pNarrationElementOutput);
		}

		@Override
		protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
			int i = this.getWidth() * this.status.getvalue();
			pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), i, 177, this.getWidth(), this.getHeight());
		}

		private void loadToolTip(Util.Status status) {
			Tooltip tooltip = switch (status) {
				case FAIL -> Tooltip.create(Component.translatable("block.bettersound.jukebox.tip.fail"));
				case SUCCESS -> Tooltip.create(Component.translatable("block.bettersound.jukebox.tip.success"));
				case NULL -> Tooltip.create(Component.translatable("block.bettersound.jukebox.tip.null"));
				case NONE -> null;
			};
			this.setTooltip(tooltip);
		}

		public void updateStatus(Util.Status status) {
			this.status = status;
			this.loadToolTip(status);
		}

		public Util.Status getStatus() {
			return status;
		}



	}
}
