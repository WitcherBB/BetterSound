package com.witcherbb.bettersound.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.blocks.ToneBlock;
import com.witcherbb.bettersound.blocks.entity.ToneBlockEntity;
import com.witcherbb.bettersound.client.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToneRenderer implements BlockEntityRenderer<ToneBlockEntity> {
    @Override
    public void render(ToneBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        Player player = Minecraft.getInstance().player;
        if (level != null) {
            if (player != null && Utils.aimedBlock(player, pBlockEntity.getBlockPos(), ToneBlock.class)) {
                pPoseStack.pushPose();
                pPoseStack.translate(0.5, 1.5, 0.5);
                pPoseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
                pPoseStack.scale(-0.025F, -0.025F, 0.025F);

                Font font = Minecraft.getInstance().font;
                String name = pBlockEntity.getToneName();
                float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int j = (int) (f1 * 255.0F) << 24;
                font.drawInBatch(name, (float) -font.width(name) / 2, 0.0F, 0x00ef30, false, pPoseStack.last().pose(), pBuffer, Font.DisplayMode.NORMAL, j, 15728880);
                pPoseStack.popPose();
            }
        } else {
            pPoseStack.pushPose();
            pPoseStack.scale(0.5F, 0.5F, 0.5F);
            pPoseStack.popPose();
        }
    }
}
