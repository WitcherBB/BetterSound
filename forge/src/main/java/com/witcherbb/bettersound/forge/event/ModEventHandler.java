package com.witcherbb.bettersound.forge.event;

import com.witcherbb.bettersound.BetterSoundClient;
import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.common.init.DataManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModEventHandler {
	@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ModClientEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			event.enqueueWork(BetterSoundClient::initPostRegistration);
		}

		@SubscribeEvent
		public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
			BetterSoundClient.registerParticleProviders();
		}

	}

	@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModServerEvents {
		@SubscribeEvent
		public static void onCommonSetup(FMLCommonSetupEvent event) {
		}

		@SubscribeEvent
		public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
		}
	}

	@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ForgeClientEvents {
		@SubscribeEvent
		public static void onGameShuttingDown(GameShuttingDownEvent event) {
		}

		@SubscribeEvent
		public static void onLevelUnload(LevelEvent.Unload event) {
		}

		@SubscribeEvent
		public static void onClientCommandRegister(RegisterClientCommandsEvent event) {
		}
	}

	@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeServerEvents {
		@SubscribeEvent
		public static void onCommandRegister(RegisterCommandsEvent event) {
		}

		@SubscribeEvent
		public static void onLevelLoad(LevelEvent.Load event) {
		}

		@SubscribeEvent
		public static void onLevelSave(LevelEvent.Save event) {
			DataManager.save();
		}

		@SubscribeEvent
		public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		}

		@SubscribeEvent
		public static void onServerStarting(ServerStartingEvent event) {
		}

		@SubscribeEvent
		public static void onServerEnding(ServerStoppingEvent event) {
		}

		@SubscribeEvent
		public static void onServerStopped(ServerStoppedEvent event) {
		}

		@SubscribeEvent
		public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
		}

		@SubscribeEvent
		public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
		}
	}

}
