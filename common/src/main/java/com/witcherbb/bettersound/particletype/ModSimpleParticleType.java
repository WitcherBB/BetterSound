package com.witcherbb.bettersound.particletype;

import net.minecraft.core.particles.SimpleParticleType;

/**
 * {@link SimpleParticleType} 的构造器在 vanilla 里是 {@code protected}：Forge 把它 patch 成了 public，
 * Fabric 那边一般靠 access widener 打开。
 *
 * <p>common 模块编译的是**纯 vanilla**（MDG 用的是 {@code mcpVersion}，没有 Forge patch），
 * 所以这里用一个子类把构造器暴露出来——protected 成员对子类可见，不依赖任何 loader 专属手段，
 * Forge / Fabric 两边都能编。
 */
public final class ModSimpleParticleType extends SimpleParticleType {

    public ModSimpleParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }
}
