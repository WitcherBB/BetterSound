package com.witcherbb.bettersound;

import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import com.witcherbb.bettersound.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/**
 * 创造模式物品栏。
 *
 * <p>留在 forge 侧：Forge 把 {@code CreativeModeTab.builder()} 补成了无参版本（自动分配页签位置），
 * 而 vanilla 只提供 {@code builder(Row, int)}，common 侧编译不过。
 */
public final class ModCreativeTabs {

    public static RegistryRef<CreativeModeTab> BETTERSOUND_TAB;

    public static void register(ModRegistrar registrar) {
        BETTERSOUND_TAB = registrar.register(Registries.CREATIVE_MODE_TAB, "bettersound", () ->
                CreativeModeTab.builder()
                        .icon(() -> ModItems.ITEM_TUNER.get().getDefaultInstance())
                        .displayItems((parameters, output) -> {
                            //完整方块
                            output.accept(ModItems.ITEM_JUKEBOX_CONTROLLER.get());
                            output.accept(ModItems.ITEM_TONE_BLOCK.get());
                            //不完整方块(越往下越不完整)
                            output.accept(ModItems.ITEM_PIANO_BLOCK.get());
                            output.accept(ModItems.ITEM_PIANO_STOOL_BLOCK.get());
                            output.accept(ModItems.ITEM_SUSTAIN_PEDAL.get());
                            //唱片
                            for (RegistryRef<Item> item : ModItems.music_discs_list) {
                                output.accept(item.get());
                            }
                            //工具
                            output.accept(ModItems.ITEM_TUNER.get());
                            //其他非方块物品
                            output.accept(ModItems.ITEM_PIANO_VOICE_CORE.get());
                            output.accept(ModItems.ITEM_KEYBOARD.get());
                            output.accept(ModItems.ITEM_WHITE_KEY.get());
                            output.accept(ModItems.ITEM_BLACK_KEY.get());
                        })
                        .title(Component.translatable("creativetab.bettersound.title"))
                        .build());
    }

    private ModCreativeTabs() {
    }
}
