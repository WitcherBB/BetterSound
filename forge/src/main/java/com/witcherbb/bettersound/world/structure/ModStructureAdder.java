package com.witcherbb.bettersound.world.structure;

import com.mojang.datafixers.util.Pair;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.world.structure.village.ModPlainsVillage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
        StructureTemplatePool pool = templatePoolRegistry.get(poolURL);
        if (pool == null) return;

        Set<Map.Entry<String, Integer>> entrySet = pieces.entrySet();
        List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(pool.rawTemplates);
        for (Map.Entry<String, Integer> entry : entrySet) {
            String pieceURL = entry.getKey();
            int weight = entry.getValue();
            SinglePoolElement piece = SinglePoolElement.legacy(pieceURL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
            for (int j = 0; j < weight; j++) {
                pool.templates.add(piece);
            }
            rawTemplates.add(new Pair<>(piece, weight));
        }
        pool.rawTemplates = rawTemplates;
    }

    public static void bootstrap(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry) {
        ModPlainsVillage.bootstrap(templatePoolRegistry, processorListRegistry);
        MusicHouse.bootstrap(templatePoolRegistry, processorListRegistry);
    }
}
