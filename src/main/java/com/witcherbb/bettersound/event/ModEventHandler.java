package com.witcherbb.bettersound.event;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.ModBlockEntityTypes;
import com.witcherbb.bettersound.client.gui.screen.inventory.*;
import com.witcherbb.bettersound.client.renderer.blockentity.PianoRenderer;
import com.witcherbb.bettersound.common.init.DataManager;
import com.witcherbb.bettersound.items.ModItems;
import com.witcherbb.bettersound.items.TunerItem;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import com.witcherbb.bettersound.client.particles.particle.BlackNoteParticle;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModEventHandler {
	@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	static class ModClientEvents {
		@SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MenuScreens.register(ModMenuTypes.JUKEBOX_MENU.get(), JukeboxScreen::new);
            MenuScreens.register(ModMenuTypes.EXAMPLE_MENU.get(), ExampleScreen::new);
			MenuScreens.register(ModMenuTypes.JUKEBOX_CONTROLLER_MENU.get(), JukeboxControllerScreen::new);
			MenuScreens.register(ModMenuTypes.NOTE_BLOCK_MENU.get(), NoteBlockScreen::new);
			MenuScreens.register(ModMenuTypes.PIANO_BLOCK_MENU.get(), PianoBlockScreen::new);

			BlockEntityRenderers.register(ModBlockEntityTypes.PIANO_BLOCK_ENTITY_TYPE.get(), ctx -> new PianoRenderer());
        }

		@SubscribeEvent
		public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
			event.registerSpriteSet(ModParticleTypes.BLACK_NOTE.get(), BlackNoteParticle.BlackNoteParticleFactory::new);
		}

		@SubscribeEvent
		public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
			if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
				event.accept(ModItems.ITEM_CONFINE_PEDAL);
			}
		}


	}

	@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	static class ModServerEvents {

		@SubscribeEvent
		public static void onCommonSetup(FMLCommonSetupEvent event) {
			ModNetwork.register();
		}

		@SubscribeEvent
		public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {

		}
	}

	@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	static class ForgeServerEvents {
		@SubscribeEvent
		public static void onLevelLoad(LevelEvent.Load event) {
			Level level = (Level) event.getLevel();
			if (level.getLevelData() instanceof PrimaryLevelData levelData) {
				BetterSound.currentLevelName = levelData.getLevelName();
			}
		}

		@SubscribeEvent
		public static void onServerStarting(ServerStartingEvent event) {
			DataManager.init();
		}

		@SubscribeEvent
		public static void onServerEnding(ServerStoppingEvent event) {
			DataManager.save();
		}

		@SubscribeEvent
		public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
			if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
				BlockPos pos = event.getPos();
				Level level = event.getLevel();
				Player player = event.getEntity();
				BlockState state = event.getLevel().getBlockState(event.getPos());

				if (!level.isClientSide && state.getBlock() instanceof NoteBlock noteBlock && player.getItemInHand(event.getHand()).getItem() instanceof TunerItem) {
					if (state.getValue(NoteBlock.INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir()) {
						level.blockEvent(pos, noteBlock, 0, 0);
						level.gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, pos);
					}
					player.awardStat(Stats.PLAY_NOTEBLOCK);
				}
			}
		}


	}

}
