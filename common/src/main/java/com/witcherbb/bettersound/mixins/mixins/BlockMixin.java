package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.mixins.extenders.BlockExtender;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(Block.class)
public abstract class BlockMixin implements BlockExtender {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/StateDefinition$Builder;create(Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/StateDefinition$Factory;)Lnet/minecraft/world/level/block/state/StateDefinition;"))
    public <O, S extends StateHolder<O, S>> StateDefinition<O, S> bettersound$init(StateDefinition.Builder<O, S> builder, Function<O, S> stateValueFunction, StateDefinition.Factory<O, S> stateFunction) {
        return builder.create(stateValueFunction, (StateDefinition.Factory<O, S>) this.getStateFactory());
    }
}
