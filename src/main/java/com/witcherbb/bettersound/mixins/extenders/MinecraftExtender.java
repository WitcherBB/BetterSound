package com.witcherbb.bettersound.mixins.extenders;

import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.music.nbs.NBSLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MinecraftExtender {

    ModSoundManager betterSound$getmodSoundManager();

    NBSLoader betterSound$getNBSLoader();
}
