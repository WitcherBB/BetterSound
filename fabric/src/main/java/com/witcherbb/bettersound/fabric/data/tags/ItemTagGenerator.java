package com.witcherbb.bettersound.fabric.data.tags;

import com.witcherbb.bettersound.items.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {

    public ItemTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, FabricTagProvider.BlockTagProvider blockTagProvider) {
        super(output, lookupProvider, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (RegistrySupplier<Item> registryObject : ModItems.music_discs_list) {
            this.getOrCreateTagBuilder(ItemTags.MUSIC_DISCS).add(registryObject.getKey());
        }
    }

    @Override
    public @NotNull String getName() {
        return "BetterSound Mod Item Tags";
    }

}
