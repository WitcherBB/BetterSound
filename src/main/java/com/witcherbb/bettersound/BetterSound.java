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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BetterSound.MODID)
public class BetterSound
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bettersound";
    public static final String VERSION = "1.20.1-0.0.0.0-demo";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static String currentLevelName = "";

    public BetterSound()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);

        ModCreativeTabs.CREATIVE_MODE_TAB_DEFERRED_REGISTER.register(modEventBus);
        
        ModItems.ITEMS.register(modEventBus);

        ModBlocks.BLOCKS.register(modEventBus);

        ModSoundEvents.SOUNDEVENTS.register(modEventBus);

        ModMenuTypes.MENUS.register(modEventBus);

        ModBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);

        ModParticleTypes.register(modEventBus);

    }
}
