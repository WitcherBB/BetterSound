package com.witcherbb.bettersound.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModLevelRenderer extends LevelRenderer {
    protected final Minecraft minecraft;

    public ModLevelRenderer(Minecraft pMinecraft, EntityRenderDispatcher pEntityRenderDispatcher, BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, RenderBuffers pRenderBuffers) {
        super(pMinecraft, pEntityRenderDispatcher, pBlockEntityRenderDispatcher, pRenderBuffers);
        this.minecraft = pMinecraft;
    }

    @Override
    public void tick() {
        super.tick();
    }
}
