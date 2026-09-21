package com.witcherbb.bettersound.items;

import net.minecraft.sounds.SoundEvent;

public class RecordItem extends net.minecraft.world.item.RecordItem {
    protected RecordItem(int analogOutput, SoundEvent sound, Properties properties, int lengthInSeconds) {
        super(analogOutput, sound, properties, lengthInSeconds);
    }
}
