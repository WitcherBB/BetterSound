package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.ExampleBlockEntity;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.SExampleNameChangedPacket;
import com.witcherbb.bettersound.menu.inventory.ExampleMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExampleScreen extends AbstractContainerScreen<ExampleMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(BetterSound.MODID, "textures/gui/example.png");
	private EditBox editBox;

	protected int menuWidth = 176;
	protected int menuHeight = 166;

	private final ExampleBlockEntity blockEntity;

	public ExampleScreen(ExampleMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		blockEntity = pMenu.getBlockEntity();
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos = (width - menuWidth) / 2;
		this.topPos = (height - menuHeight) / 2;
		this.editBox = new EditBox(this.font, this.leftPos + 56, this.topPos + 89, 64, 10, Component.translatable("block.bettersound.jukebox.name_input"));
		this.editBox.setMaxLength(50);
		this.editBox.setTextColor(0xFFFFFF);
		this.editBox.setCanLoseFocus(true);
		this.editBox.setValue(blockEntity.getUpdateTag().getString("Name"));
		this.addWidget(this.editBox);
		this.addRenderableWidget(new ExampleConfirmButton());
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		renderBackground(pGuiGraphics);
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);

		pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.menuWidth, this.menuHeight);
		this.editBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}

	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		super.resize(pMinecraft, pWidth, pHeight);
	}

	@Override
	protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (this.editBox.isFocused()) {
			return this.editBox.keyPressed(pKeyCode, pScanCode, pModifiers);
		} else {
			return super.keyPressed(pKeyCode, pScanCode, pModifiers);
		}
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		this.editBox.setFocused(false);
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	class ExampleConfirmButton extends AbstractButton {

		public ExampleConfirmButton() {
			super(leftPos + 121, topPos + 83, 22, 22, CommonComponents.GUI_DONE);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
			this.defaultButtonNarrationText(pNarrationElementOutput);
		}

		@Override
		public void onPress() {
			ModNetwork.sendToServer(new SExampleNameChangedPacket(ExampleScreen.this.editBox.getValue()));
		}

		@Override
		protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
			int xOffset = 0;

			if (this.isHovered()) {
				xOffset = this.width * 2;
			}

			pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), xOffset, 166, this.width, this.height);
			pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), 66, 166, this.width, this.height);
		}
	}
}
