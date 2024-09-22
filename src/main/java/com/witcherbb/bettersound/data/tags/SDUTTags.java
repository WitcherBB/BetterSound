package com.witcherbb.bettersound.data.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class SDUTTags {
    public static class Items {
        public static final TagKey<Item> RECORD_ITEMS = ItemTags.create(new ResourceLocation("music_discs"));
    }
}
