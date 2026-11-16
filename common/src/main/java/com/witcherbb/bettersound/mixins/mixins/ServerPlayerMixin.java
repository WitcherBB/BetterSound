package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.blocks.entity.utils.Sittable;
import com.witcherbb.bettersound.mixins.extenders.ServerPlayerExtender;
import com.witcherbb.bettersound.mixins.internal.PlayerInvoker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends LivingEntity implements ServerPlayerExtender, PlayerInvoker {

    @Shadow public ServerGamePacketListenerImpl connection;
    @Unique
    protected Player betterSound$self = (Player) (Object) this;

    protected ServerPlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean betterSound$startSitting(Sittable<?> pSeat) {
        if (!this.bs$startSittingF(pSeat)) {
            return false;
        } else {
            pSeat.positionPassenger(betterSound$self);
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            return true;
        }
    }

    @Override
    public void betterSound$stopSitting() {
        this.bs$stopSittingF();
        this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
    }
}
