package com.witcherbb.bettersound.common.registry;

import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

/**
 * 注册器：把 common 侧的注册请求转交给具体 loader。
 *
 * <p>各 loader 提供实现：Forge 用 {@code DeferredRegister}（见 forge 模块的 {@code ForgeRegistrar}），
 * 其它 loader 可以直接注册。注册表用 vanilla 的 {@link net.minecraft.core.registries.Registries}
 * 里的 key 指定，这样 common 不需要认识任何 loader 的注册表对象。
 */
public interface ModRegistrar {

    /**
     * 注册一个条目。
     *
     * @param registry 目标注册表，例如 {@code Registries.BLOCK}
     * @param name     条目的路径名（命名空间固定为 {@code Constants.MOD_ID}）
     * @param factory  条目工厂
     * @param <T>      注册表里登记的类型
     * @param <R>      具体类型，必须是 {@code T} 的子类型
     * @return 绑定到本次注册结果的引用
     */
    <T, R extends T> RegistryRef<R> register(ResourceKey<? extends Registry<T>> registry, String name, Supplier<R> factory);

    /**
     * 注册一个菜单类型。
     *
     * <p>菜单需要「打开时额外数据」（例如方块坐标），这在 vanilla 里没有对应 API，
     * 所以由各 loader 用自己的机制实现（Forge 是 {@code IForgeMenuType}）。
     */
    <T extends AbstractContainerMenu> RegistryRef<MenuType<T>> registerMenu(String name, MenuFactory<T> factory);

    /** 创建菜单的工厂：{@code extraData} 由打开方写入，可能为 {@code null}。 */
    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int windowId, Inventory inventory, @Nullable FriendlyByteBuf extraData);
    }
}
