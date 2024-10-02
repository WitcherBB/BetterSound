package com.witcherbb.bettersound.mixins.extenders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SoundInstanceExtender {
    void betterSound$onStop();
}
