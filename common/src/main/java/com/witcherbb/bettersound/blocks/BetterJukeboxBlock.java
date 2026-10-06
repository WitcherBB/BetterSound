package com.witcherbb.bettersound.blocks;

import com.mojang.serialization.MapCodec;
import com.witcherbb.bettersound.blocks.entity.ModBlockEntityTypes;
import com.witcherbb.bettersound.common.platform.Platform;
import com.witcherbb.bettersound.mixins.extenders.BlockExtender;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.witcherbb.bettersound.blocks.entity.BetterJukeboxBlockEntity;
import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BetterJukeboxBlock extends BaseEntityBlock implements BlockExtender {
    public static final BooleanProperty HAS_RECORD = BooleanProperty.create("has_record");

	private static final VoxelShape betterSound$OUTSIDE = Shapes.block();
	private static final VoxelShape betterSound$INSIDE = Block.box(1.0D, 14.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    protected BetterJukeboxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, false));
    }

	@Override
	public StateDefinition.Factory<Block, BlockState> getStateFactory() {
		return BetterJukeboxBlockState::new;
	}

	@Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BetterJukeboxBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override 
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.join(betterSound$OUTSIDE, betterSound$INSIDE, BooleanOp.ONLY_FIRST);
    }

    @Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		if (!pLevel.isClientSide) {
			BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
			if (blockEntity instanceof BetterJukeboxBlockEntity jukeboxBlockEntity) {
				CompoundTag nbt = BlockItem.getBlockEntityData(pStack);
				if (nbt != null) {
					nbt.putBoolean("IsPlaying", false);
					jukeboxBlockEntity.load(nbt);
					String name = nbt.getString("Name");
					if (!name.isEmpty()) {
						JukeboxControllerBlockEntity.putPos(name, pLevel.dimension().location().getPath(), pPos, pLevel);
					}
				}
			}
		}
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		BlockEntity entity = pLevel.getBlockEntity(pPos);
		if (!pLevel.isClientSide()) {
			if (entity instanceof BetterJukeboxBlockEntity jukeboxBlockEntity) {
				if (!jukeboxBlockEntity.isRecordPlaying()) {
					Platform.hooks().openMenu((ServerPlayer) pPlayer, jukeboxBlockEntity);
				}
			} else {
				throw new IllegalStateException("Our Container provider is missing");
			}
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, ModBlockEntityTypes.BETTER_JUKEBOX_BLOCK_ENTITY_TYPE.get(), BetterJukeboxBlockEntity::tick);
	}

	static class BetterJukeboxBlockState extends BlockState {
		public BetterJukeboxBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> values, MapCodec<BlockState> propertiesCodec) {
			super(block, values, propertiesCodec);
		}

		@Override
		public void onRemove(Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
			if (!this.is(newState.getBlock())) {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof BetterJukeboxBlockEntity jukeboxBlockEntity) {
					jukeboxBlockEntity.onRemove();
					level.updateNeighbourForOutputSignal(pos, this.getBlock());
					CompoundTag nbt = level.getBlockEntity(pos).getUpdateTag();
					JukeboxControllerBlockEntity.removePos(nbt.getString("Name"), level.dimension().location().getPath(), pos, level);
				}
			}
			super.onRemove(level, pos, newState, movedByPiston);
		}
	}
}
