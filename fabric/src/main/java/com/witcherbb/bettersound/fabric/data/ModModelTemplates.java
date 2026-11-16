package com.witcherbb.bettersound.fabric.data;

import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

final class ModModelTemplates {
    static final ModelTemplate GENERATED_FLAT_ITEM = new ModelTemplate(
            Optional.of(new ResourceLocation("item/generated")),
            Optional.empty(),
            TextureSlot.LAYER0
    );
}
