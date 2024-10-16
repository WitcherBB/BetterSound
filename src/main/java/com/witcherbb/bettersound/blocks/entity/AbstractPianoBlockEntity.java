package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.client.gui.screen.inventory.AbstractPianoScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractPianoBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {
    private static final String[] keyNames = new String[]{
            "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"
    };
    public static final Map<Integer, String> toneNameMap = new TreeMap<>();
    private boolean isSoundDelay = false;
    @OnlyIn(Dist.CLIENT)
    private int firstWhiteKey = 39;

    static {
        for (int i = 0; i < 88; i++) {
            toneNameMap.put(i, getComponent(i).getString());
        }
    }

    public AbstractPianoBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
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
        if (this.level == null || this.level.isClientSide) return;
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.bettersound.piano_block.title");
    }

    public boolean isSoundDelay() {
        return this.isSoundDelay;
    }

    public void setSoundDelay(boolean soundDelay) {
        isSoundDelay = soundDelay;
    }

    @OnlyIn(Dist.CLIENT)
    public int getFirstWhiteKey() {
        return this.firstWhiteKey;
    }

    @OnlyIn(Dist.CLIENT)
    public void updateFirstWhiteKey(AbstractPianoScreen screen) {
        this.firstWhiteKey = screen.getFirstWhiteKey();
    }

    private static Component getComponent(int id) {
        int length = keyNames.length;

        if (id < 0 || id >= 88) {
            return Component.literal("Invalid key ID");
        } else if (id < 3) {
            return switch (id) {
                case 0 -> Component.literal("A0");
                case 1 -> Component.literal("A#/Bb0");
                case 2 -> Component.literal("B0");
                default -> Component.empty();
            };
        }

        int effectiveId = id % length;
        int depth = id / length + 1;

        return Component.literal(keyNames[effectiveId] + depth);
    }
}
