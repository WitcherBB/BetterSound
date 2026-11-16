package com.witcherbb.bettersound.particletype;

import com.witcherbb.bettersound.Constants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public final class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Constants.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> BLACK_NOTE =
            PARTICLES.register("black_note", () -> new ModSimpleParticleType(true));

    private ModParticleTypes() {
    }

    public static void register() {
        PARTICLES.register();
    }
}
