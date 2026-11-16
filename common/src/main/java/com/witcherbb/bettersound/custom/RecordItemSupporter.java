package com.witcherbb.bettersound.custom;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.List;

public interface RecordItemSupporter {
    List<RegistrySupplier<SoundEvent>> registerSoundEvents(DeferredRegister<SoundEvent> soundEvents);

    List<RegistrySupplier<Item>> registerItems(DeferredRegister<Item> items);

    void acceptCreativeTab(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output);
}
