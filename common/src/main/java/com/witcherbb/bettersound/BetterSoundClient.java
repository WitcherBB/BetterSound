package com.witcherbb.bettersound;

import com.witcherbb.bettersound.blocks.entity.ModBlockEntityTypes;
import com.witcherbb.bettersound.client.ModOptions;
import com.witcherbb.bettersound.client.particles.particle.BlackNoteParticle;
import com.witcherbb.bettersound.client.renderer.blockentity.ToneRenderer;
import com.witcherbb.bettersound.client.sound.ModSoundManager;
import com.witcherbb.bettersound.commands.Commands;
import com.witcherbb.bettersound.items.ModItems;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public class BetterSoundClient {
    public static void init() {
        registerEventsHandler();
        // 创造模式物品栏注册
        ModCreativeTabs.register();
        buildCreativeModeTabContents();

        ModSoundManager.INSTANCE.getNbsLoader().load();
        ModSoundManager.INSTANCE.getMidiLoader().load();
    }

    private static void registerEventsHandler() {
        ClientLifecycleEvent.CLIENT_STARTED.register(EventHandlers::onClientStarted);
        ClientLifecycleEvent.CLIENT_STOPPING.register(EventHandlers::onClientStopping);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(EventHandlers::OnPlayerQuit);
        ClientCommandRegistrationEvent.EVENT.register(Commands::registerOnClient);
    }

    private static void buildCreativeModeTabContents() {
        CreativeTabRegistry.append(ModCreativeTabs.VanillaTabs.REDSTONE_BLOCKS,
                ModItems.ITEM_SUSTAIN_PEDAL,
                ModItems.ITEM_TONE_BLOCK);
        CreativeTabRegistry.append(ModCreativeTabs.VanillaTabs.INGREDIENTS,
                ModItems.ITEM_PIANO_VOICE_CORE,
                ModItems.ITEM_KEYBOARD,
                ModItems.ITEM_WHITE_KEY,
                ModItems.ITEM_BLACK_KEY);
    }

    public static void initPostRegistration() {
        ModMenuTypes.registerScreenHandler();
        BlockEntityRendererRegistry.register(ModBlockEntityTypes.TONE_BLOCK_ENTITY_TYPE.get(),
                ctx -> new ToneRenderer());
    }

    public static void registerParticleProviders() {
        ParticleProviderRegistry.register(ModParticleTypes.BLACK_NOTE.get(), BlackNoteParticle.BlackNoteParticleFactory::new);
    }

    static final class EventHandlers {
        static void onClientStarted(Minecraft mc) {
            ModOptions.getOptions().load();
        }

        static void onClientStopping(Minecraft mc) {
        }

        static void OnPlayerQuit(LocalPlayer levelAccessor) {
            ModSoundManager.INSTANCE.destroy();
            ModOptions.getOptions().save();
        }
    }
}
