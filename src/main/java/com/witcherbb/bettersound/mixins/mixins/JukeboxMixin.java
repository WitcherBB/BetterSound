package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;
import com.witcherbb.bettersound.blocks.extensions.SpectatorInvalidBlock;
import com.witcherbb.bettersound.blocks.utils.Tickers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = JukeboxBlock.class)
public abstract class JukeboxMixin extends BaseEntityBlock implements SpectatorInvalidBlock {

	@Unique
	private static final VoxelShape betterSound$OUTSIDE = Shapes.block();
	@Unique
	private static final VoxelShape betterSound$INSIDE = Block.box(1.0D, 14.0D, 1.0D, 15.0D, 16.0D, 15.0D);

	@Final
	@Shadow public static BooleanProperty HAS_RECORD;

	@Unique
	private static final BooleanProperty betterSound$TRIGGERED = BlockStateProperties.TRIGGERED;

	protected JukeboxMixin(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return Shapes.join(betterSound$OUTSIDE, betterSound$INSIDE, BooleanOp.ONLY_FIRST);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		if (!pLevel.isClientSide) {
			BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
			if (blockEntity instanceof JukeboxBlockEntity jukeboxBlockEntity) {
				CompoundTag nbt = BlockItem.getBlockEntityData(pStack);
				if (nbt != null) {
					nbt.putBoolean("IsPlaying", false);
					jukeboxBlockEntity.load(nbt);
					String name = nbt.getString("Name");
					if (!name.isEmpty()) {
						JukeboxControllerBlockEntity.putPos2List(name, pLevel.dimension().location().getPath(), pPos);
					}
				}
			}
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injected(CallbackInfo info) {
		this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)).setValue(betterSound$TRIGGERED, Boolean.valueOf(false)));
	}

	@Inject(method = "onRemove", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/entity/JukeboxBlockEntity;popOutRecord()V"
	))
	public void onRemove0(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving, CallbackInfo ci) {
		CompoundTag nbt = pLevel.getBlockEntity(pPos).getUpdateTag();
		JukeboxControllerBlockEntity.removeFromList(nbt.getString("Name"), pLevel.dimension().location().getPath(), pPos);
	}

	@Inject(method = "createBlockStateDefinition", at = @At("HEAD"), cancellable = true)
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder, CallbackInfo ci) {
		pBuilder.add(betterSound$TRIGGERED, HAS_RECORD);
		ci.cancel();
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		BlockEntity entity = pLevel.getBlockEntity(pPos);
		if (!pLevel.isClientSide()) {
			if (entity instanceof JukeboxBlockEntity jukeboxBlockEntity) {
				if (!jukeboxBlockEntity.isRecordPlaying()) {
					NetworkHooks.openScreen(((ServerPlayer) pPlayer), (MenuProvider) jukeboxBlockEntity, pPos);
				}
			} else {
				throw new IllegalStateException("Our Container provider is missing");
			}
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}

	@Inject(method = "getTicker", at = @At("HEAD"), cancellable = true)
	public <T extends BlockEntity> void getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
		if (!pLevel.isClientSide())
			cir.setReturnValue(
					pState.getValue(HAS_RECORD) ?
							createTickerHelper(pBlockEntityType, BlockEntityType.JUKEBOX, JukeboxBlockEntity::playRecordTick) :
							createTickerHelper(pBlockEntityType, BlockEntityType.JUKEBOX, Tickers::jukeboxTicker)
			);
		else cir.setReturnValue(null);
	}
}
