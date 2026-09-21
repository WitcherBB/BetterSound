package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.blocks.entity.utils.Sittable;
import com.witcherbb.bettersound.mixins.extenders.ServerPlayerExtender;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerSittingMixin implements ServerPlayerExtender {

    @Shadow public ServerGamePacketListenerImpl connection;

    protected ServerPlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean betterSound$startSitting(Sittable<?> pSeat) {
        if (!super.betterSound$startSitting(pSeat)) {
            return false;
        } else {
            pSeat.positionPassenger(betterSound$self);
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            return true;
        }
    }

    @Override
    public void betterSound$stopSitting() {
        super.betterSound$stopSitting();
        this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
    }
}
