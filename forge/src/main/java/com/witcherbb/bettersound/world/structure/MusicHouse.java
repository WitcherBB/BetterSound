package com.witcherbb.bettersound.world.structure;

import com.witcherbb.bettersound.BetterSound;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class MusicHouse {
    private static final String PARROT_PREFIX = "bettersound:music_house/parrot_%s";

    public static void bootstrap(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry) {
        ModStructureAdder.addBuildings(templatePoolRegistry, processorListRegistry,
                new ResourceLocation(BetterSound.MODID, "music_house"),
                new NbtPieceBuilder()
                        .add("bettersound:music_house/house", 150)
                        .add(parrotColor("blue"), 1)
                        .add(parrotColor("cyan"), 1)
                        .add(parrotColor("gray"), 1)
                        .add(parrotColor("green"), 1)
                        .add(parrotColor("red"), 1)
                        .build());
    }

    private static String parrotColor(String color) {
        return PARROT_PREFIX.formatted(color);
    }
}
