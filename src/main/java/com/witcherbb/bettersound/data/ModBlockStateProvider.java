package com.witcherbb.bettersound.data;

import com.witcherbb.bettersound.BetterSound;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BetterSound.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }
}
