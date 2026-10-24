package com.witcherbb.bettersound.client.gui.screen.inventory;

import net.minecraft.core.BlockPos;

public class ToneBlockScreen extends AbstractPianoScreen {
    public ToneBlockScreen(BlockPos pos) {
        super(pos);
    }

    @Override
    protected BlockPos getMainPos(BlockPos currentPos) {
        return currentPos;
    }
}
