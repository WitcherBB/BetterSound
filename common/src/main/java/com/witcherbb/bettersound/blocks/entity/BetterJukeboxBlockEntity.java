package com.witcherbb.bettersound.blocks.entity;

import com.witcherbb.bettersound.blocks.BetterJukeboxBlock;
import com.witcherbb.bettersound.menu.ExtendedMenuProvider;
import com.witcherbb.bettersound.menu.inventory.BetterJukeboxMenu;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BetterJukeboxBlockEntity extends BlockEntity implements ContainerSingleItem, ExtendedMenuProvider {
    @NotNull ItemStack recordItemStack = ItemStack.EMPTY;

    private int ticksSinceLastEvent;
    private boolean isPlaying;
    private long tickCount;
    private long recordStartedTick;
    private String name = "";

    public BetterJukeboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BETTER_JUKEBOX_BLOCK_ENTITY_TYPE.get(), pos, blockState);
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("RecordItem", 10)) {
            this.setFirstItem(ItemStack.of(tag.getCompound("RecordItem")));
        }

        this.isPlaying = tag.getBoolean("IsPlaying");
        this.recordStartedTick = tag.getLong("RecordStartTick");
        this.tickCount = tag.getLong("TickCount");
        this.name = tag.getString("Name");
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.getFirstItem().isEmpty()) {
            tag.put("RecordItem", this.getFirstItem().save(new CompoundTag()));
        }

        tag.putBoolean("IsPlaying", this.isPlaying);
        tag.putLong("RecordStartTick", this.recordStartedTick);
        tag.putLong("TickCount", this.tickCount);
        tag.putString("Name", this.name);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt);
        return nbt;
    }

    private void setHasRecordBlockState(@Nullable Entity entity, boolean hasRecord) {
        if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(BetterJukeboxBlock.HAS_RECORD, Boolean.valueOf(hasRecord)), 2);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
        }
    }

    /**
     * 调用该方法的几种情况:
     * <ul>
     *     <li>{@link com.witcherbb.bettersound.blocks.JukeboxControllerBlock}控制</li>
     *     <li>UI界面控制</li>
     *     <li>播放顺序控制</li>
     * </ul>
     */
    public void startPlaying() {
        this.recordStartedTick = this.tickCount;
        this.isPlaying = true;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent((Player)null, 1010, this.getBlockPos(), Item.getId(this.getFirstItem().getItem()));
        this.setChanged();
    }

    /**
     * 调用该方法的几种情况:
     * <ul>
     *     <li>{@link com.witcherbb.bettersound.blocks.JukeboxControllerBlock}控制</li>
     *     <li>UI界面控制</li>
     *     <li>音乐播放结束</li>
     * </ul>
     */
    public void stopPlaying() {
        this.isPlaying = false;
        this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(1011, this.getBlockPos(), 0);
        this.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BetterJukeboxBlockEntity blockEntity) {
        ++blockEntity.ticksSinceLastEvent;
        if (blockEntity.isRecordPlaying()) {
            if (blockEntity.getFirstItem().getItem() instanceof RecordItem recordItem) {
                if (blockEntity.shouldRecordStopPlaying(recordItem)) {
                    blockEntity.stopPlaying();
                } else if (blockEntity.shouldSendJukeboxPlayingEvent()) {
                    blockEntity.ticksSinceLastEvent = 0;
                    level.gameEvent(GameEvent.JUKEBOX_PLAY, pos, GameEvent.Context.of(state));
                    blockEntity.spawnMusicParticles(level, pos);
                }
            }
        }

        ++blockEntity.tickCount;
    }

    private Boolean shouldRecordStopPlaying(RecordItem record) {
        return this.tickCount >= this.recordStartedTick + record.getLengthInTicks() + 20;
    }

    private boolean shouldSendJukeboxPlayingEvent() {
        return this.ticksSinceLastEvent >= 20;
    }

    public Boolean isRecordPlaying() {
        return !this.getFirstItem().isEmpty() && this.isPlaying;
    }

    public void onRemove() {
        if (this.level != null) {
            this.stopPlaying();
            Containers.dropContents(this.level, this.worldPosition, this);
        }
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return slot == 0 ? recordItemStack : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ItemStack.EMPTY;
        if (slot == 0) {
            itemStack = ContainerHelper.removeItem(this.getItems(), slot, amount);
        }
        return itemStack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            this.setHasRecordBlockState(null, stack != ItemStack.EMPTY);
            recordItemStack = stack;
        }
    }

    private List<ItemStack> getItems() {
        return List.of(this.recordItemStack);
    }

    private void spawnMusicParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverlevel) {
            Vec3 vec3 = Vec3.atBottomCenterOf(pos).add(0.0D, (double)1.2F, 0.0D);
            float f = (float)level.getRandom().nextInt(4) / 24.0F;
            serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0D, 0.0D, 1.0D);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.bettersound.better_jukebox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BetterJukeboxMenu(i, inventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
