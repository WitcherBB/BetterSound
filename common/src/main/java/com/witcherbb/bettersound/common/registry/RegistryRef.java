package com.witcherbb.bettersound.common.registry;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

/**
 * 与 loader 无关的「注册表条目引用」。
 *
 * <p>common 侧的内容类只持有它，不关心底层是 Forge 的 {@code RegistryObject} 还是
 * Fabric 的直接注册结果；绑定动作由各 loader 的 {@link ModRegistrar} 实现完成。
 *
 * <p>典型用法：内容是 {@code public static RegistryRef<PianoBlock> PIANO_BLOCK;}，
 * 在 {@code register(ModRegistrar)} 里赋值，业务代码用 {@code PIANO_BLOCK.get()}。
 */
public final class RegistryRef<T> implements Supplier<T> {

    private Supplier<T> supplier;
    private Supplier<ResourceLocation> resourceLocationSupplier;

    /** 由 loader 侧的注册实现调用；重复绑定视为编程错误。 */
    public void bind(Supplier<T> supplier, Supplier<ResourceLocation> resourceSupplier) {
        if (this.supplier != null) {
            throw new IllegalStateException("RegistryRef 已经绑定过了");
        }
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.resourceLocationSupplier = Objects.requireNonNull(resourceSupplier, "resourceSupplier");
    }

    @Override
    public T get() {
        if (this.supplier == null) {
            throw new IllegalStateException("RegistryRef 尚未绑定：mod 还没走完注册流程");
        }
        return this.supplier.get();
    }

    public ResourceLocation getId() {
        if (this.resourceLocationSupplier == null) {
            throw new IllegalStateException("RegistryRef 尚未绑定：mod 还没走完注册流程");
        }
        return this.resourceLocationSupplier.get();
    }

    public boolean isBound() {
        return this.supplier != null;
    }

    @Override
    public String toString() {
        return "RegistryRef[" + (isBound() ? "bound" : "unbound") + "]";
    }
}
