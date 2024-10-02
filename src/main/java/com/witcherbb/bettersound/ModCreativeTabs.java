package com.witcherbb.bettersound;

import com.witcherbb.bettersound.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB_DEFERRED_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BetterSound.MODID);
    public static final RegistryObject<CreativeModeTab> SDUT_TAB = CREATIVE_MODE_TAB_DEFERRED_REGISTER.register("sdut", () ->
            CreativeModeTab.builder()
                    .icon(() -> ModItems.ITEM_TUNER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        //完整方块
                        output.accept(ModItems.ITEM_JUKEBOX_CONTROLLER.get());
                        output.accept(ModItems.ITEM_PIANO_BLOCK.get());
                        output.accept(ModItems.ITEM_TONE_BLOCK.get());
                        //不完整方块(越往下越不完整)
                        output.accept(ModItems.ITEM_PIANO_STOOL_BLOCK.get());
                        output.accept(ModItems.ITEM_CONFINE_PEDAL.get());
                        //唱片
                        int size = ModItems.music_discs_list.size();
                        for (int i = 0; i < size; i++) {
                            RegistryObject<Item> registryObject = ModItems.music_discs_list.get(i);
                            output.accept(registryObject.get());
                        }
                        //其他非方块物品
                        output.accept(ModItems.ITEM_TUNER.get());
                    })
                    .title(Component.translatable("creativetab.bettersound.title"))
                    .build());
}
