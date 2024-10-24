package com.witcherbb.bettersound.blocks.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.common.data.JukeboxEntityDataProvider;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import com.witcherbb.bettersound.menu.inventory.JukeboxControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JukeboxControllerBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {
	public static final Codec<List<BlockPos>> POS_CODEC = BlockPos.CODEC.listOf();
	private String name = "";
	private static final JukeboxEntityDataProvider provider = new JukeboxEntityDataProvider();
	private List<BlockPos> jukeboxPoses = new ArrayList<>();

	public static JukeboxEntityDataProvider getProvider() {
		return provider;
	}

	public JukeboxControllerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntityTypes.JUKEBOX_CONTROLLER_ENTITY_TYPE.get(), pos, state);
	}

	@Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.bettersound.jukebox_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new JukeboxControllerMenu(containerId, inventory, this, null);
    }
	@Override
	public void tick() {
		if (this.level == null || this.level.isClientSide())
			return;
		this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
	}

	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		this.name = nbt.getString("Name");
		this.jukeboxPoses = POS_CODEC.parse(NbtOps.INSTANCE, nbt.get("ConnectedJukebox")).result().orElseGet(ArrayList::new);
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putString("Name", this.name);
		nbt.put("ConnectedJukebox",
				POS_CODEC.encodeStart(NbtOps.INSTANCE, this.jukeboxPoses).result().orElseGet(CompoundTag::new));
		if (this.level != null && !this.level.isClientSide) {
			provider.updateToFile();
		}
    }

	@Override
	public @NotNull CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Name", this.name);
		nbt.put("ConnectedJukebox",
				POS_CODEC.encodeStart(NbtOps.INSTANCE, this.jukeboxPoses).result().orElseGet(CompoundTag::new));
		return nbt;
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public void startplayMusic() {
        if (this.level != null && this.level.isClientSide) {
			int size = this.jukeboxPoses.size();
			for (int i = 0; i < size; i++) {
				Optional<JukeboxBlockEntity> blockEntity = this.level.getBlockEntity(this.jukeboxPoses.get(i), BlockEntityType.JUKEBOX);
                blockEntity.ifPresent(JukeboxBlockEntity::startPlaying);
			}
		}
	}

	public void stopPlayMusic() {
		if (this.level != null && this.level.isClientSide) {
			int size = this.jukeboxPoses.size();
			for (int i = 0; i < size; i++) {
				Optional<JukeboxBlockEntity> blockEntity = this.level.getBlockEntity(this.jukeboxPoses.get(i), BlockEntityType.JUKEBOX);
				blockEntity.ifPresent(JukeboxBlockEntity::stopPlaying);
			}
		}
	}

	public static void putPos2List(String name, String dimension, BlockPos blockPos) {
		JukeboxEntityData.Pos pos = new JukeboxEntityData.Pos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		//TODO provider统一管理各个名称的控制器，便于唯一检查。数据存放方法不变，不再写进json文件，而是写进nbs文件，自定义Codec。

		provider.addPos(name, dimension, pos);
	}

	public static void removeFromList(String name, String dimension, BlockPos blockPos) {
		JukeboxEntityData.Pos pos = new JukeboxEntityData.Pos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		provider.removePos(name, dimension, pos);
	}

	public static void removeControllerAndJukebox(String name, Level level, BlockPos blockPos) {
		BlockEntity entity = level.getBlockEntity(blockPos);
		if (entity instanceof JukeboxControllerBlockEntity) {
			JukeboxEntityDataProvider provider = JukeboxControllerBlockEntity.getProvider();
			JukeboxEntityData.Pos[] posArray = provider.getPosListByNameAndDimension(name, level.dimension().location().getPath()).toArray(new JukeboxEntityData.Pos[0]);
			if (provider.removeControllerPos(name, level.dimension().location().getPath(), JukeboxEntityData.createPos(blockPos))) {
				int size = posArray.length;
				for (int i = 0; i < size; i++) {
					JukeboxEntityData.Pos pos = posArray[i];
					BlockPos blockPos1 = new BlockPos(pos.x(), pos.y(), pos.z());
					BlockEntity blockEntity = level.getBlockEntity(blockPos1);
					if (blockEntity instanceof JukeboxBlockEntity jukeboxBlockEntity) {
						CompoundTag nbt = jukeboxBlockEntity.getUpdateTag();
						nbt.putString("Name", "");
						jukeboxBlockEntity.load(nbt);
					}
				}
			}
		}
	}

	public String getName() {
		return name;
	}
}
