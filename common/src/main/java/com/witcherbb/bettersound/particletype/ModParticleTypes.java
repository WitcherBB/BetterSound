package com.witcherbb.bettersound.particletype;

import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

/**
 * 粒子类型。与 loader 无关：注册实际由各 loader 的 {@link ModRegistrar} 实现完成。
 *
 * <p>注意工厂显式声明成 {@code Supplier<SimpleParticleType>}，这样引用类型就是
 * {@code RegistryRef<SimpleParticleType>}，与注册方（{@code ParticleProvider<SimpleParticleType>}）一致。
 */
public final class ModParticleTypes {

    public static RegistryRef<SimpleParticleType> BLACK_NOTE;

    /** 由各 loader 的入口在注册阶段调用一次。 */
    public static void register(ModRegistrar registrar) {
        Supplier<SimpleParticleType> blackNote = () -> new ModSimpleParticleType(true);
        BLACK_NOTE = registrar.register(Registries.PARTICLE_TYPE, "black_note", blackNote);
    }

    private ModParticleTypes() {
    }
}
