package com.witcherbb.bettersound.world.structure.village;

import com.witcherbb.bettersound.world.structure.ModStructureAdder;
import com.witcherbb.bettersound.world.structure.NbtPieceBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class ModPlainsVillage {
    public static void bootstrap(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry) {
        ModStructureAdder.addBuildings(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("village/plains/houses"),
                new NbtPieceBuilder().add("bettersound:village/music", 150).build());
    }
}
