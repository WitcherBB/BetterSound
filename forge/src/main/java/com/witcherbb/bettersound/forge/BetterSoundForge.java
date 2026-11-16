package com.witcherbb.bettersound.forge;

import com.witcherbb.bettersound.BetterSoundClient;
import com.witcherbb.bettersound.BetterSoundCommon;
import com.witcherbb.bettersound.Constants;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MOD_ID)
public final class BetterSoundForge
{
    /** @deprecated 已迁移到 common 的 {@link Constants#MOD_ID}，保留别名只为兼容尚未迁移的代码。 */
    @Deprecated
    public static final String MODID = Constants.MOD_ID;
    /** @deprecated 已迁移到 {@link Constants#VERSION}。 */
    @Deprecated
    public static final String VERSION = Constants.VERSION;
    /** @deprecated 已迁移到 {@link Constants#LOGGER}。 */
    @Deprecated
    public static final Logger LOGGER = Constants.LOGGER;

    private static BetterSoundForge instance;

    public BetterSoundForge()
    {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(MODID, modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeCommonConfig.commonSpec);
        BetterSoundCommon.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig.clientSpec);
            BetterSoundClient.init();
        });
    }

    public static BetterSoundForge getInstance() {
        return instance;
    }
}
