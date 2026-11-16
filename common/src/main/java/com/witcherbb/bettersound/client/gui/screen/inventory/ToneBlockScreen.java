package com.witcherbb.bettersound.client.gui.screen.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class ToneBlockScreen extends AbstractPianoScreen {
    public ToneBlockScreen(BlockPos pos) {
        super(pos);
    }

    @Override
    protected BlockPos getMainPos(BlockPos currentPos) {
        return currentPos;
    }
}
