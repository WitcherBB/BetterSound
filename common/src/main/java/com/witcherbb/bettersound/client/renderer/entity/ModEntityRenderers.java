package com.witcherbb.bettersound.client.renderer.entity;

import com.witcherbb.bettersound.entities.ModEntities;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.NoopRenderer;

@Environment(EnvType.CLIENT)
public class ModEntityRenderers {
    public static void register() {
        EntityRendererRegistry.register(ModEntities.FAKE_STOOL_ENTITY, NoopRenderer::new);
    }
}
