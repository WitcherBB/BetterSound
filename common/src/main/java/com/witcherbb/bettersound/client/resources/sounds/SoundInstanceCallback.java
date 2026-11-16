package com.witcherbb.bettersound.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SoundInstanceCallback {
    void onStop();
}
