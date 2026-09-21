package com.witcherbb.bettersound.client.particles.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class BlackNoteParticle extends TextureSheetParticle {
    protected BlackNoteParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.hasPhysics = false;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;
        this.rCol = 0.3F;
        this.gCol = 0.3F;
        this.bCol = 0.3F;
        this.quadSize = 0.15F * ((float) this.random.nextInt(0, 40) / 100 + 1.0F);
        this.lifetime = 6;
    }

    protected BlackNoteParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        this(pLevel, pX, pY, pZ, 0, 0, 0);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        super.render(pBuffer, pRenderInfo, pPartialTicks);
    }

    public static class BlackNoteParticleFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public BlackNoteParticleFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(@NotNull SimpleParticleType pType, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            BlackNoteParticle blackNoteParticle = new BlackNoteParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            blackNoteParticle.pickSprite(this.spriteSet);
            return blackNoteParticle;
        }
    }


}
