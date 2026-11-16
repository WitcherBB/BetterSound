package com.witcherbb.bettersound.world.structure;

import com.witcherbb.bettersound.mixins.mixins.StructureTemplatePoolAccessor;
import com.witcherbb.bettersound.world.structure.village.ModPlainsVillage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class ModStructureAdder {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, new ResourceLocation("empty")
    );

    public static void addBuildings(Registry<StructureTemplatePool> templatePoolRegistry,
                                    Registry<StructureProcessorList> processorListRegistry,
                                    ResourceLocation poolURL,
                                    @NotNull Map<String, Integer> pieces) {
        if (templatePoolRegistry == null || processorListRegistry == null || pieces.isEmpty()) return;

        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        StructureTemplatePoolAccessor pool = (StructureTemplatePoolAccessor) templatePoolRegistry.get(poolURL);
        if (pool == null) return;

        Set<Map.Entry<String, Integer>> entrySet = pieces.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            String pieceURL = entry.getKey();
            int weight = entry.getValue();
            SinglePoolElement piece = SinglePoolElement.legacy(pieceURL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
            for (int j = 0; j < weight; j++) {
                pool.bs$templates().add(piece);
            }
        }
    }

    public static void bootstrap(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry) {
        ModPlainsVillage.bootstrap(templatePoolRegistry, processorListRegistry);
//        MusicHouse.bootstrap(templatePoolRegistry, processorListRegistry);
    }
}
