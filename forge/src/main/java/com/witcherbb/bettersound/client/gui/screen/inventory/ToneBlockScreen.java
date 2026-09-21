package com.witcherbb.bettersound.client.gui.screen.inventory;

import com.witcherbb.bettersound.menu.inventory.AbstractPianoMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToneBlockScreen extends AbstractPianoScreen {
    public ToneBlockScreen(AbstractPianoMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }
}
