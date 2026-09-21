package com.witcherbb.bettersound;

import com.mojang.logging.LogUtils;
import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.entity.ModBlockEntityTypes;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.items.ModItems;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import com.witcherbb.bettersound.common.config.Configs;
import com.witcherbb.bettersound.common.platform.Platform;
import com.witcherbb.bettersound.config.ForgeConfigValues;
import com.witcherbb.bettersound.network.ForgeNetwork;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.platform.ForgeLoaderHooks;
import com.witcherbb.bettersound.registry.ForgeRegistrar;
import com.witcherbb.bettersound.menu.ForgeMenuTypes;
import com.witcherbb.bettersound.menu.ModMenuTypes;

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
public final class BetterSound
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

    private static BetterSound instance;

    public BetterSound()
    {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.commonSpec);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.clientSpec);
        });
        // loader 差异注入（common 侧只认接口）
        Platform.install(new ForgeLoaderHooks());
        Configs.install(new ForgeConfigValues());
        ModNetwork.init(ForgeNetwork.BRIDGE);

        // common 侧声明的注册，通过 ForgeRegistrar 落到 DeferredRegister 上。
        // 顺序有讲究：后面的登记表会引用前面那些 RegistryRef 字段，字段必须先完成赋值。
        ForgeRegistrar registrar = new ForgeRegistrar();
        ModSoundEvents.register(registrar);
        ModBlocks.register(registrar);
        ModBlockEntityTypes.register(registrar);
        ModItems.register(registrar);
        ModCreativeTabs.register(registrar);
        ModParticleTypes.register(registrar);

        // 菜单类型还没迁移（jukebox 那两个用了 Forge 的物品能力），暂时保持原样
        ModMenuTypes.register(registrar);
        ForgeMenuTypes.register(registrar);

        registrar.registerAll(modEventBus);

    }

    public static BetterSound getInstance() {
        return instance;
    }
}
