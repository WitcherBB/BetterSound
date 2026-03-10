package com.witcherbb.bettersound.blocks.entity;

import com.mojang.serialization.Codec;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.common.data.impl.JukeboxEntityDataProvider;
import com.witcherbb.bettersound.menu.inventory.JukeboxControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import net.minecraftforge.server.ServerLifecycleHooks;
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
		this.jukeboxPoses = new ArrayList<>(POS_CODEC.parse(NbtOps.INSTANCE, nbt.get("ConnectedJukebox")).result().orElseGet(ArrayList::new));
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
                blockEntity.ifPresent(jukeboxBlockEntity -> jukeboxBlockEntity.setFirstItem(jukeboxBlockEntity.getFirstItem()));
			}
		}
	}

	public void stopPlayMusic() {
		if (this.level != null && this.level.isClientSide) {
			int size = this.jukeboxPoses.size();
			for (int i = 0; i < size; i++) {
				Optional<JukeboxBlockEntity> blockEntityOptional = this.level.getBlockEntity(this.jukeboxPoses.get(i), BlockEntityType.JUKEBOX);
				blockEntityOptional.ifPresent(be -> be.removeItem(100, be.getMaxStackSize()));
			}
		}
	}

	public static void putPos(String name, String dimension, BlockPos blockPos, Level level) {
		// provider统一管理各个名称的控制器，便于唯一检查。数据存放方法不变，不再写进json文件，而是写进nbs文件，自定义Codec。
		provider.addPos(name, dimension, blockPos);
		var ctrlList = provider.get(name, dimension).getControllerPosList();
		for (BlockPos ctPos : ctrlList) {
			var be = level.getBlockEntity(ctPos);
			if (be instanceof JukeboxControllerBlockEntity ctBE)
				ctBE.jukeboxPoses.add(blockPos);
		}
	}

	public static void removePos(String name, String dimension, BlockPos blockPos, Level level) {
		// 直接在this.jukeboxPoses里面remove
		var jdDT = provider.get(name, dimension);
		jdDT.removeBlockPos(blockPos);
		var ctrlList = jdDT.getControllerPosList();
		for (BlockPos ctPos : ctrlList) {
			var be = level.getBlockEntity(ctPos);
			if (be instanceof JukeboxControllerBlockEntity ctBE)
				ctBE.jukeboxPoses.remove(blockPos);
		}
	}

	public static void removeControllerAndJukebox(String name, @NotNull Level level, BlockPos blockPos) {
		BlockEntity entity = level.getBlockEntity(blockPos);
		if (entity instanceof JukeboxControllerBlockEntity) {
			JukeboxEntityDataProvider provider = JukeboxControllerBlockEntity.getProvider();
			BlockPos[] posArray = provider.getPosListByNameAndDimension(name, level.dimension().location().getPath()).toArray(BlockPos[]::new);
			if (provider.removeControllerPos(name, level.dimension().location().getPath(), blockPos)) {
				int size = posArray.length;
				for (int i = 0; i < size; i++) {
					BlockPos pos = posArray[i];
					BlockEntity blockEntity = level.getBlockEntity(pos);
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
