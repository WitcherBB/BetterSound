package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.network.protocol.SJukeboxControllerNamePacket;
import com.witcherbb.bettersound.menu.inventory.JukeboxControllerMenu;
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
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class JukeboxControllerScreen extends AbstractContainerScreen<JukeboxControllerMenu> {
	public static final ResourceLocation TEXTURE = new ResourceLocation(BetterSound.MODID, "textures/gui/jukebox_controller.png");
	private final JukeboxControllerBlockEntity blockEntity;

	private EditBox editBox;

	public JukeboxControllerScreen(JukeboxControllerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.blockEntity = menu.getBlockEntity();
	}

	@Override
	protected void init() {
		this.imageWidth = 147;
		this.imageHeight = 85;
		super.init();
		this.editBox = new EditBox(this.font, this.leftPos + 42, this.topPos + 30, 64, 10, Component.translatable("block.bettersound.jukebox.name_input"));
		this.editBox.setMaxLength(50);
		this.editBox.setTextColor(0xFFFFFF);
		this.editBox.setCanLoseFocus(true);
		this.editBox.setMaxLength(30);
		this.editBox.setValue(blockEntity.getUpdateTag().getString("Name"));
		this.addWidget(this.editBox);
		this.addRenderableWidget(new JukeboxControllerConfirmButton());
	}

	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		String s = this.editBox.getValue();
		this.init(pMinecraft, pWidth, pHeight);
		this.editBox.setValue(s);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);

		guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);
		this.editBox.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
		guiGraphics.drawString(this.font, this.editBox.getMessage(), this.titleLabelX + 7, this.titleLabelY + 25, 4210752, false);
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

	class JukeboxControllerConfirmButton extends AbstractButton {

		public JukeboxControllerConfirmButton() {
			super(JukeboxControllerScreen.this.editBox.getX() + JukeboxControllerScreen.this.editBox.getWidth() + 5,
					JukeboxControllerScreen.this.editBox.getY(),
					10, 10, CommonComponents.GUI_DONE);
		}

		@Override
		public void onPress() {
			ModNetwork.sendToServer(new SJukeboxControllerNamePacket(JukeboxControllerScreen.this.editBox.getValue(), JukeboxControllerScreen.this.blockEntity.getLevel().dimension().location().getPath()));
		}

		@Override
		protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
			this.defaultButtonNarrationText(pNarrationElementOutput);
		}

		@Override
		protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
			int xOffset = 0;

			if (this.isHovered()) {
				xOffset = this.width;
			}

			pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), xOffset, 93, this.width, this.height);
			pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), 20, 93, this.width, this.height);
		}
	}
}
