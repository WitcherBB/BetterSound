package com.witcherbb.bettersound.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.blocks.entity.PianoBlockEntity;
import com.witcherbb.bettersound.client.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PianoRenderer implements BlockEntityRenderer<PianoBlockEntity> {
    @Override
    public void render(PianoBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        Player player = Minecraft.getInstance().player;
        if (level != null) {
            if (player != null && Utils.aimedBlock(player, pBlockEntity.getBlockPos(), PianoBlock.class)) {
                BlockState state = level.getBlockState(pBlockEntity.getBlockPos());
                Direction facing = state.getValue(PianoBlock.FACING);
                Vec3 offsetVec = switch (state.getValue(PianoBlock.PART)) {
                    case PEDAL -> new Vec3(0.0, 1.0, 0.0);
                    case PEDAL_R -> new Vec3(0.0, 1.0, 0.0).relative(facing.getClockWise(), 1.0);
                    case KEYBOARD_R -> new Vec3(0.0, 0.0, 0.0).relative(facing.getClockWise(), 1.0);
                    case KEYBOARD_M -> new Vec3(0.0, 0.0, 0.0);
                    case KEYBOARD_L -> new Vec3(0.0, 0.0, 0.0).relative(facing.getCounterClockWise(), 1.0);
                    case PEDAL_L -> new Vec3(0.0, 1.0, 0.0).relative(facing.getCounterClockWise(), 1.0);
                };
                pPoseStack.pushPose();
                pPoseStack.translate(0.5 + offsetVec.x, 1.5 + offsetVec.y, 0.5 + offsetVec.z);
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
