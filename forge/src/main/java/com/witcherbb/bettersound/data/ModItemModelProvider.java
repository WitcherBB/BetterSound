package com.witcherbb.bettersound.data;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import com.witcherbb.bettersound.items.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (RegistryRef<Item> registryRef : ModItems.music_discs_list) {
            simpleRecordItem(registryRef);
        }
    }

    private ItemModelBuilder simpleRecordItem(RegistryRef<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(Constants.MOD_ID, "item/sdut_music_disc"));
    }
}
