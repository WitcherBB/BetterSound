package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.menu.inventory.PianoBlockMenu;
import com.witcherbb.bettersound.music.midi.MidiPlayer;
import com.witcherbb.bettersound.music.nbs.AutoPlayer;
import com.witcherbb.bettersound.music.nbs.NBSPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PianoBlockEntity extends AbstractPianoBlockEntity implements AutoPlayer {
    private final NBSPlayer nbsPlayer;

    public PianoBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.PIANO_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
        this.nbsPlayer = new NBSPlayer(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level != null && !this.level.isClientSide) {
            this.nbsPlayer.tick();
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.bettersound.piano_block.title");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new PianoBlockMenu(pContainerId, pPlayerInventory, this, null);
    }

    @Override
    public NBSPlayer getNBSPlayer() {
        return this.nbsPlayer;
    }

    @Override
    public MidiPlayer getMidiPlayer() {
        return null;
    }

    @Override
    public void playNBSOn() {
        this.nbsPlayer.playOn();
    }

    @Override
    public void stopNBS() {
        this.nbsPlayer.stop();
    }

    @Override
    public void pauseNBS() {
        this.nbsPlayer.pause();
    }

    @Override
    public void playMidiOn() {

    }

    @Override
    public void stopMidi() {

    }

    @Override
    public void pauseMidi() {

    }
}
