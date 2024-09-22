package com.witcherbb.bettersound.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;

@OnlyIn(Dist.CLIENT)
public class ModOptions {
    private final File optionsFile;

    public ModOptions(File pGameDirectory) {
        this.optionsFile = new File(pGameDirectory, "options.txt");
    }
}
