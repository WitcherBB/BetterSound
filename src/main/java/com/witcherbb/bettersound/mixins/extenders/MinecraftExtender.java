package com.witcherbb.bettersound.mixins.extenders;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MinecraftExtender {

    ModSoundManager getmodSoundManager();

}
