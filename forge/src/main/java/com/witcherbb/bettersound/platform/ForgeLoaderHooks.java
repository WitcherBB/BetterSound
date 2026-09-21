package com.witcherbb.bettersound.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.witcherbb.bettersound.common.platform.BlockEntityFactory;
import com.witcherbb.bettersound.common.platform.LoaderHooks;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/** Forge 侧的 loader 差异实现。 */
public final class ForgeLoaderHooks implements LoaderHooks {

    @Override
    public int onNoteChange(Level level, BlockPos pos, BlockState state, int oldNote, int newNote) {
        return ForgeHooks.onNoteChange(level, pos, state, oldNote, newNote);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        NetworkHooks.openScreen(player, provider, pos);
    }

    @Override
    @Nullable
    public MinecraftServer currentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntityFactory<T> factory, Block... validBlocks) {
        return BlockEntityType.Builder.of(factory::create, validBlocks).build(null);
    }

    @Override
    public KeyMapping createGuiKeyMapping(String name, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, KeyConflictContext.GUI, type, keyCode, category);
    }

    @Override
    public String serializeKeyMapping(KeyMapping mapping) {
        return mapping.saveString() + (mapping.getKeyModifier() != KeyModifier.NONE ? ":" + mapping.getKeyModifier() : "");
    }

    @Override
    public void applySerializedKeyMapping(KeyMapping mapping, String serialized) {
        if (serialized.indexOf(':') != -1) {
            String[] parts = serialized.split(":");
            mapping.setKeyModifierAndCode(KeyModifier.valueFromString(parts[1]), InputConstants.getKey(parts[0]));
        } else {
            mapping.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.getKey(serialized));
        }
    }
}
