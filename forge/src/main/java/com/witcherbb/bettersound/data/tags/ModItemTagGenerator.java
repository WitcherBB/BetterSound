package com.witcherbb.bettersound.data.tags;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import com.witcherbb.bettersound.items.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {

    public ModItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (RegistryRef<Item> registryObject : ModItems.music_discs_list) {
            this.tag(ItemTags.MUSIC_DISCS).add(registryObject.get());
        }
    }

    @Override
    public @NotNull String getName() {
        return "SDUT Mod Item Tags";
    }

}
