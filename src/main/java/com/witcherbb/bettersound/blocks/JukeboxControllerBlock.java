package com.witcherbb.bettersound.blocks;

import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;
import com.witcherbb.bettersound.blocks.entity.utils.TickableBlockEntity;
import com.witcherbb.bettersound.blocks.extensions.SpectatorInvalidBlock;
import com.witcherbb.bettersound.common.data.JukeboxEntityDataProvider;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JukeboxControllerBlock extends BaseEntityBlock implements SpectatorInvalidBlock {
    public static final BooleanProperty OPENED = BooleanProperty.create("opened");

    public JukeboxControllerBlock() {
        super(Properties.copy(Blocks.STONE));
        this.registerDefaultState(getStateDefinition().any().setValue(OPENED, false));
    }

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		if (!pLevel.isClientSide) {
			BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
			if (blockEntity instanceof JukeboxControllerBlockEntity jukeboxControllerBlockEntity) {
				CompoundTag nbt = BlockItem.getBlockEntityData(pStack);
				if (nbt != null) {
					jukeboxControllerBlockEntity.load(nbt);
					String name = nbt.getString("Name");
					if (!name.isEmpty()) {
						JukeboxControllerBlockEntity.getProvider().addControllerPos(name, pLevel.dimension().location().getPath(), JukeboxEntityData.createPos(pPos));
					}
				}
			}
		}
	}

	@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        BlockEntity entity = level.getBlockEntity(pos);
		if (!level.isClientSide()){
			if (entity instanceof JukeboxControllerBlockEntity jukeboxControllerBlockEntity) {
				NetworkHooks.openScreen((ServerPlayer) player, jukeboxControllerBlockEntity, pos);
			} else {
				throw new IllegalStateException("Our Container provider is missing");
			}
		}
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborBlockPos, boolean movedByPiston) {
		boolean flag = hasNeighborSignalExceptJukebox(level, pos);
		boolean flag1 = state.getValue(OPENED);
		if (flag != flag1) {
			JukeboxControllerBlockEntity blockEntity = (JukeboxControllerBlockEntity) level.getBlockEntity(pos);
			if (blockEntity != null) {
				if (flag && !checkAnyOpened(blockEntity.getName(), level)) {
					level.setBlock(pos, state.setValue(OPENED, true), Block.UPDATE_ALL);
					startPlaying(blockEntity.getName(), level);

				} else {
					level.setBlock(pos, state.setValue(OPENED, false), Block.UPDATE_ALL);
					stopPlaying(blockEntity.getName(), level);
				}
			}
		}

    }

	private static boolean hasNeighborSignalExceptJukebox(Level level, BlockPos pos) {
		if (!(level.getBlockState(pos.below()).getBlock() instanceof JukeboxBlock) && level.getSignal(pos.below(), Direction.DOWN) > 0) {
			return true;
		} else if (!(level.getBlockState(pos.above()).getBlock() instanceof JukeboxBlock) && level.getSignal(pos.above(), Direction.UP) > 0) {
			return true;
		} else if (!(level.getBlockState(pos.north()).getBlock() instanceof JukeboxBlock) && level.getSignal(pos.north(), Direction.NORTH) > 0) {
			return true;
		} else if (!(level.getBlockState(pos.south()).getBlock() instanceof JukeboxBlock) && level.getSignal(pos.south(), Direction.SOUTH) > 0) {
			return true;
		} else if (!(level.getBlockState(pos.west()).getBlock() instanceof JukeboxBlock) && level.getSignal(pos.west(), Direction.WEST) > 0) {
			return true;
		} else {
			return !(level.getBlockState(pos.east()).getBlock() instanceof JukeboxBlock) && level.getSignal(pos.east(), Direction.EAST) > 0;
		}
	}

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof JukeboxControllerBlockEntity jukeboxControllerBlockEntity && !pState.is(pNewState.getBlock())) {
			String name = jukeboxControllerBlockEntity.getUpdateTag().getString("Name");
			stopPlaying(name, pLevel);
			JukeboxControllerBlockEntity.removeControllerAndJukebox(name, pLevel, pPos);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JukeboxControllerBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPENED);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.createTicker();
    }

    public static void startPlaying(String name, Level level) {
		List<JukeboxEntityData.Pos> posList = JukeboxControllerBlockEntity.getProvider().getPosListByNameAndDimension(name, level.dimension().location().getPath());
		int size = posList.size();
		for (int i = 0; i < size; i++) {
			JukeboxBlockEntity jukeboxBlockEntity = JukeboxEntityData.getBlockEntity(level, posList.get(i));
			if (jukeboxBlockEntity != null) {
				//play
				jukeboxBlockEntity.setFirstItem(jukeboxBlockEntity.getFirstItem());
//				jukeboxBlockEntity.startPlaying();
			}
		}
	}

	public static void stopPlaying(String name, Level level) {
        JukeboxEntityData.Pos[] posArray = JukeboxControllerBlockEntity.getProvider().getPosListByNameAndDimension(name, level.dimension().location().getPath()).toArray(new JukeboxEntityData.Pos[0]);
		int size = posArray.length;
		for (int i = 0; i < size; i++) {
			JukeboxBlockEntity jukeboxBlockEntity = JukeboxEntityData.getBlockEntity(level, posArray[i]);
			if (jukeboxBlockEntity != null) {
				//stop
				jukeboxBlockEntity.removeItem(100, 0);
//				jukeboxBlockEntity.stopPlaying();
			}
		}
	}

	public static boolean checkAnyOpened(String name, Level level) {
		JukeboxEntityDataProvider provider = JukeboxControllerBlockEntity.getProvider();
		JukeboxEntityData.Pos[] posArray = provider.getControllerPosListByNameAndDimension(name, level.dimension().location().getPath()).toArray(new JukeboxEntityData.Pos[0]);
		int size = posArray.length;
		for (int i = 0; i < size; i++) {
			if (level.getBlockState(posArray[i].toBlockPos()).getValue(JukeboxControllerBlock.OPENED)) {
				return true;
			}
		}
		return false;
	}
}
