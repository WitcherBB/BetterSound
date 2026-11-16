package com.witcherbb.bettersound;

import com.witcherbb.bettersound.blocks.ModBlocks;
import com.witcherbb.bettersound.blocks.entity.ModBlockEntityTypes;
import com.witcherbb.bettersound.blocks.extensions.SpectatorInvalidBlock;
import com.witcherbb.bettersound.common.ModToneManager;
import com.witcherbb.bettersound.common.events.ModSoundEvents;
import com.witcherbb.bettersound.common.init.DataManager;
import com.witcherbb.bettersound.custom.RecordItemSupporter;
import com.witcherbb.bettersound.custom.registries.RecordItemRegistry;
import com.witcherbb.bettersound.items.ModItems;
import com.witcherbb.bettersound.menu.ModMenuTypes;
import com.witcherbb.bettersound.network.ModNetwork;
import com.witcherbb.bettersound.particletype.ModParticleTypes;
import com.witcherbb.bettersound.world.structure.ModStructureAdder;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.PrimaryLevelData;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class BetterSoundCommon {
    public static final RecordItemRegistry RECORD_ITEM_REGISTRY = new RecordItemRegistry();

    public static void init() {
        // 加载插件
        ServiceLoader.load(RecordItemSupporter.class).forEach(RECORD_ITEM_REGISTRY::register);
        RECORD_ITEM_REGISTRY.freeze();
        // 网络包注册
        ModNetwork.register();

        registerEventsHandler();
        ModSoundEvents.register();
        ModMenuTypes.register();
        ModParticleTypes.register();
        ModBlocks.register();
        ModItems.register();
        ModBlockEntityTypes.register();
    }

    private static void registerEventsHandler() {
        LifecycleEvent.SERVER_BEFORE_START.register(EventHandlers::onServerBeforeStart);
        LifecycleEvent.SERVER_LEVEL_LOAD.register(EventHandlers::onLevelLoad);
        LifecycleEvent.SERVER_STOPPED.register(EventHandlers::onServerStopped);
        InteractionEvent.LEFT_CLICK_BLOCK.register(EventHandlers::onLeftClickBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(EventHandlers::onRightClickBlock);
    }

    static final class EventHandlers {
        static void onServerBeforeStart(MinecraftServer server) {
            var templatePoolRegistry = server.registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
            var processorListRegistry = server.registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();

            ModStructureAdder.bootstrap(templatePoolRegistry, processorListRegistry);
        }

        public static void onServerStopped(MinecraftServer server) {
            ModToneManager.destroy();
        }

        static void onLevelLoad(ServerLevel level) {
            ModToneManager.getInstance();

            if (level.getLevelData() instanceof PrimaryLevelData levelData) {
                DataManager.init(levelData.getLevelName());
            }
        }

        static EventResult onLeftClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
            Level level = player.level();
            BlockState state = level.getBlockState(pos);
            if (!level.isClientSide && state.is(Blocks.NOTE_BLOCK) && player.getItemInHand(hand).is(ModItems.ITEM_TUNER.get())) {
                if (state.getValue(NoteBlock.INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir()) {
                    level.blockEvent(pos, Blocks.NOTE_BLOCK, 0, 0);
                    level.gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, pos);
                }
                player.awardStat(Stats.PLAY_NOTEBLOCK);
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        }

        static EventResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
            Level level = player.level();
            if (player.isSpectator() && level.getBlockState(pos).getBlock() instanceof SpectatorInvalidBlock) {
                return EventResult.interruptDefault();
            }
            return EventResult.pass();
        }
    }
}
