package com.witcherbb.bettersound;

import com.google.common.base.Suppliers;
import com.witcherbb.bettersound.items.ModItems;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

/**
 * 创造模式物品栏。
 *
 * <p>留在 forge 侧：Forge 把 {@code CreativeModeTab.builder()} 补成了无参版本（自动分配页签位置），
 * 而 vanilla 只提供 {@code builder(Row, int)}，common 侧编译不过。
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Constants.MOD_ID, Registries.CREATIVE_MODE_TAB);

    private static final Supplier<RegistrySupplier<CreativeModeTab>> BETTERSOUND_TAB = Suppliers.memoize(() ->
            CREATIVE_TABS.register("bettersound", () ->
                CreativeTabRegistry.create(builder ->
                    builder.icon(() -> ModItems.ITEM_TUNER.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                //完整方块
                                output.accept(ModItems.ITEM_BETTER_JUKEBOX.get());
                                output.accept(ModItems.ITEM_JUKEBOX_CONTROLLER.get());
                                output.accept(ModItems.ITEM_TONE_BLOCK.get());
                                //不完整方块(越往下越不完整)
                                output.accept(ModItems.ITEM_PIANO_BLOCK.get());
                                output.accept(ModItems.ITEM_PIANO_STOOL_BLOCK.get());
                                output.accept(ModItems.ITEM_SUSTAIN_PEDAL.get());
                                //工具
                                output.accept(ModItems.ITEM_TUNER.get());
                                //其他非方块物品
                                output.accept(ModItems.ITEM_PIANO_VOICE_CORE.get());
                                output.accept(ModItems.ITEM_KEYBOARD.get());
                                output.accept(ModItems.ITEM_WHITE_KEY.get());
                                output.accept(ModItems.ITEM_BLACK_KEY.get());
                                //唱片
                                BetterSoundCommon.RECORD_ITEM_REGISTRY.forEach(supporter -> {
                                    supporter.acceptCreativeTab(parameters, output);
                                });
                            })
                            .title(Component.translatable("creativetab.bettersound.title"))
                            .build()
                )
            ));

    public static void register() {
        BETTERSOUND_TAB.get();
        CREATIVE_TABS.register();
    }

    public static RegistrySupplier<CreativeModeTab> getModTab() {
        return BETTERSOUND_TAB.get();
    }

    private ModCreativeTabs() {
    }

    public static final class VanillaTabs {
        public static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS =
                key("building_blocks");
        public static final ResourceKey<CreativeModeTab> COLORED_BLOCKS =
                key("colored_blocks");
        public static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS =
                key("natural_blocks");
        public static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS =
                key("functional_blocks");
        public static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS =
                key("redstone_blocks");
        public static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES =
                key("tools_and_utilities");
        public static final ResourceKey<CreativeModeTab> COMBAT =
                key("combat");
        public static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS =
                key("food_and_drinks");
        public static final ResourceKey<CreativeModeTab> INGREDIENTS =
                key("ingredients");
        public static final ResourceKey<CreativeModeTab> SPAWN_EGGS =
                key("spawn_eggs");
        public static final ResourceKey<CreativeModeTab> OP_BLOCKS =
                key("op_blocks");

        private static ResourceKey<CreativeModeTab> key(String path) {
            return ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    new ResourceLocation("minecraft", path)
            );
        }

        private VanillaTabs() {
        }
    }
}
