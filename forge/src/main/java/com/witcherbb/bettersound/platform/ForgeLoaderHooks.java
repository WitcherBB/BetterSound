package com.witcherbb.bettersound.platform;

import com.witcherbb.bettersound.common.platform.BlockEntityFactory;
import com.witcherbb.bettersound.common.platform.LoaderHooks;
import com.witcherbb.bettersound.menu.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

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
    public void openMenu(ServerPlayer player, MenuProvider provider, BiConsumer<ServerPlayer, FriendlyByteBuf> dataAdder) {
        NetworkHooks.openScreen(player, provider, buf -> dataAdder.accept(player, buf));
    }

    @Override
    public void openMenu(ServerPlayer player, ExtendedMenuProvider provider) {
        NetworkHooks.openScreen(player, provider, buf -> provider.writeScreenOpeningData(player, buf));
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
}
