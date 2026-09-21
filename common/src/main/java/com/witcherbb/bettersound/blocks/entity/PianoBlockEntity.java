package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.menu.inventory.PianoBlockMenu;
import com.witcherbb.bettersound.music.AutoMusicPlayer;
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
    /** 共用的自动演奏引擎：NBS 与 MIDI 都走它，因此两者共享播放状态、天然互斥 */
    private final AutoMusicPlayer musicPlayer;
    /** NBS 曲目入口：只负责投递 NBS 曲目，本身不持有播放状态 */
    private final NBSPlayer nbsPlayer;
    /** MIDI 曲目入口：只负责投递 MIDI 曲目，本身不持有播放状态 */
    private final MidiPlayer midiPlayer;

    public PianoBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.PIANO_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
        this.musicPlayer = new AutoMusicPlayer(this);
        this.nbsPlayer = new NBSPlayer(this, this.musicPlayer);
        this.midiPlayer = new MidiPlayer(this, this.musicPlayer);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level != null && !this.level.isClientSide) {
            this.musicPlayer.tick();
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
    public AutoMusicPlayer getMusicPlayer() {
        return this.musicPlayer;
    }

    @Override
    public NBSPlayer getNBSPlayer() {
        return this.nbsPlayer;
    }

    @Override
    public MidiPlayer getMidiPlayer() {
        return this.midiPlayer;
    }

    // 播放状态只有一份，所以下面六个方法的实现是一样的：NBS 与 MIDI 的差别只在曲目来源与延音释放策略
    @Override
    public void playNBSOn() {
        this.musicPlayer.playOn();
    }

    @Override
    public void stopNBS() {
        this.musicPlayer.stop();
    }

    @Override
    public void pauseNBS() {
        this.musicPlayer.pause();
    }

    @Override
    public void playMidiOn() {
        this.musicPlayer.playOn();
    }

    @Override
    public void stopMidi() {
        this.musicPlayer.stop();
    }

    @Override
    public void pauseMidi() {
        this.musicPlayer.pause();
    }
}
