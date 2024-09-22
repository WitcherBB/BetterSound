package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.PianoBlock;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.menu.inventory.PianoBlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class PianoBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {
    private static final String[] keyNames = new String[]{
            "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"
    };
    public static final Map<Integer, String> toneNameMap = new TreeMap<>();
    private boolean isSoundDelay = true;
    private String toneName = "";

    static {
        for (int i = 0; i < 88; i++) {
            toneNameMap.put(i, getComponent(i).getString());
        }
    }

    public PianoBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.PIANO_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("IsSoundDelay", this.isSoundDelay);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.isSoundDelay = pTag.getBoolean("IsSoundDelay");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("IsSoundDelay", this.isSoundDelay);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.isSoundDelay = tag.getBoolean("IsSoundDelay");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void tick() {
        this.toneName = toneNameMap.get(this.getBlockState().getValue(PianoBlock.TONE));
        if (this.level == null || this.level.isClientSide) return;
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.bettersound.piano_block.title");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new PianoBlockMenu(pContainerId, pPlayerInventory, this, null);
    }

    public boolean isSoundDelay() {
        return this.isSoundDelay;
    }

    public void setSoundDelay(boolean soundDelay) {
        isSoundDelay = soundDelay;
    }

    public void setToneName(String toneName) {
        this.toneName = toneName;
    }

    public String getToneName() {
        return toneName;
    }

    private static Component getComponent(int id) {
        if (id < 3) {
            return switch (id) {
                case 0 -> Component.nullToEmpty("A0");
                case 1 -> Component.nullToEmpty("A#/Bb0");
                case 2 -> Component.nullToEmpty("B0");
                default -> Component.empty();
            };
        }
        int depth = 1;
        return getFitComponent(id - 3, depth);
    }

    private static Component getFitComponent(int id, int depth) {
        int length = keyNames.length;
        if (id >= length) {
            return getFitComponent(id - length, depth + 1);
        } else {
            return Component.nullToEmpty(keyNames[id] + depth);
        }
    }
}
