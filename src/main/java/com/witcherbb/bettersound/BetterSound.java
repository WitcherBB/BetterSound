package com.witcherbb.bettersound;

import com.mojang.logging.LogUtils;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.entity.ModBlockEntityTypes;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.items.ModItems;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BetterSound.MODID)
public final class BetterSound
{
    public static final String MODID = "bettersound";
    public static final String VERSION = "1.20.1-0.0.0.0-demo";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static BetterSound instance;

    public String currentLevelName = "";

    public BetterSound()
    {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModCreativeTabs.CREATIVE_MODE_TAB_DEFERRED_REGISTER.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModSoundEvents.SOUNDEVENTS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);
        ModParticleTypes.register(modEventBus);

    }

    public static BetterSound getInstance() {
        return instance;
    }
}
