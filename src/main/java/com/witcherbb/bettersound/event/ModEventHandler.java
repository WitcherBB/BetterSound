package com.witcherbb.bettersound.event;

import com.mojang.brigadier.CommandDispatcher;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.ModBlockEntityTypes;
import com.witcherbb.bettersound.blocks.extensions.SpectatorInvalidBlock;
import com.witcherbb.bettersound.client.ModOptions;
import com.witcherbb.bettersound.client.gui.screen.inventory.*;
import com.witcherbb.bettersound.client.renderer.blockentity.ToneRenderer;
import com.witcherbb.bettersound.common.init.DataManager;
import com.witcherbb.bettersound.items.ModItems;
import com.witcherbb.bettersound.items.TunerItem;
import com.witcherbb.bettersound.mixins.extenders.MinecraftExtender;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import com.witcherbb.bettersound.client.particles.particle.BlackNoteParticle;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import com.witcherbb.bettersound.commands.ModCommands;
import com.witcherbb.bettersound.world.structure.ModStructureAdder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModEventHandler {
	@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ModClientEvents {
		@SubscribeEvent
        public void onClientSetup(FMLClientSetupEvent event) {
			ModOptions.getOptions().load();

            MenuScreens.register(ModMenuTypes.JUKEBOX_MENU.get(), JukeboxScreen::new);
            MenuScreens.register(ModMenuTypes.EXAMPLE_MENU.get(), ExampleScreen::new);
			MenuScreens.register(ModMenuTypes.JUKEBOX_CONTROLLER_MENU.get(), JukeboxControllerScreen::new);
			MenuScreens.register(ModMenuTypes.NOTE_BLOCK_MENU.get(), NoteBlockScreen::new);
			MenuScreens.register(ModMenuTypes.PIANO_BLOCK_MENU.get(), PianoBlockScreen::new);
			MenuScreens.register(ModMenuTypes.TONE_BLOCK_MENU.get(), ToneBlockScreen::new);

			BlockEntityRenderers.register(ModBlockEntityTypes.TONE_BLOCK_ENTITY_TYPE.get(), ctx -> new ToneRenderer());

			((MinecraftExtender) Minecraft.getInstance()).betterSound$getNBSLoader().load();
        }

		@SubscribeEvent
		public void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
			event.registerSpriteSet(ModParticleTypes.BLACK_NOTE.get(), BlackNoteParticle.BlackNoteParticleFactory::new);
		}

		@SubscribeEvent
		public void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
			if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
				event.accept(ModItems.ITEM_SUSTAIN_PEDAL);
				event.accept(ModItems.ITEM_TONE_BLOCK);
			} else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
				event.accept(ModItems.ITEM_PIANO_VOICE_CORE.get());
				event.accept(ModItems.ITEM_KEYBOARD.get());
				event.accept(ModItems.ITEM_WHITE_KEY.get());
				event.accept(ModItems.ITEM_BLACK_KEY.get());
			}
		}

		@SubscribeEvent
		public void onRegisterBindings(RegisterKeyMappingsEvent event) {

		}

	}

	@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModServerEvents {

		@SubscribeEvent
		public void onCommonSetup(FMLCommonSetupEvent event) {
			ModNetwork.register();
		}

		@SubscribeEvent
		public void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {

		}
	}

	@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ForgeClientEvents {

	}

	@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeServerEvents {
		@SubscribeEvent
		public void onCommandRegister(RegisterCommandsEvent event) {
			CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
			Commands.CommandSelection selection = event.getCommandSelection();
			CommandBuildContext context = event.getBuildContext();

			ModCommands.register(dispatcher, selection, context);
		}

		@SubscribeEvent
		public void onLevelLoad(LevelEvent.Load event) {
			Level level = (Level) event.getLevel();
			if (level.getLevelData() instanceof PrimaryLevelData levelData) {
				DataManager.init(levelData.getLevelName());
			}
		}

		@SubscribeEvent
		public void onLevelSave(LevelEvent.Save event) {
			DataManager.save();
		}

		@SubscribeEvent
		public void onServerAboutToStart(ServerAboutToStartEvent event) {
			Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
			Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();

			ModStructureAdder.bootstrap(templatePoolRegistry, processorListRegistry);
		}

		@SubscribeEvent
		public void onServerStarting(ServerStartingEvent event) {
		}

		@SubscribeEvent
		public void onServerEnding(ServerStoppingEvent event) {
		}

		@SubscribeEvent
		public void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
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

		@SubscribeEvent
		public void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
			Level level = event.getLevel();
			if (event.getEntity().isSpectator() && level.getBlockState(event.getHitVec().getBlockPos()).getBlock() instanceof SpectatorInvalidBlock) {
				event.setCanceled(true);
			}
		}
	}

}
