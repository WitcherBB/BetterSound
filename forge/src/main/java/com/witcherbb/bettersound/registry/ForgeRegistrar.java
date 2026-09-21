package com.witcherbb.bettersound.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Forge 侧实现：把 common 的注册请求转发给 {@link DeferredRegister}。
 *
 * <p>用法（在 mod 构造函数里）：
 * <pre>{@code
 * ForgeRegistrar registrar = new ForgeRegistrar();
 * ModBlocks.register(registrar);        // common 侧的声明
 * registrar.registerAll(modEventBus);   // 一次性挂到 mod 事件总线
 * }</pre>
 */
public final class ForgeRegistrar implements ModRegistrar {

    private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new HashMap<>();
    private final List<DeferredRegister<?>> ordered = new ArrayList<>();

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, R extends T> RegistryRef<R> register(ResourceKey<? extends Registry<T>> registry, String name, Supplier<R> factory) {
        DeferredRegister deferred = deferredFor((ResourceKey) registry);
        RegistryObject<R> object = (RegistryObject<R>) deferred.register(name, factory);
        RegistryRef<R> ref = new RegistryRef<>();
        ref.bind(object::get, object::getId);
        return ref;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends AbstractContainerMenu> RegistryRef<MenuType<T>> registerMenu(String name, MenuFactory<T> factory) {
        DeferredRegister deferred = deferredFor(Registries.MENU);
        MenuType<T> menuType = IForgeMenuType.create((windowId, inventory, extraData) -> factory.create(windowId, inventory, extraData));
        RegistryObject<MenuType<T>> object = (RegistryObject<MenuType<T>>) deferred.register(name, () -> menuType);
        RegistryRef<MenuType<T>> ref = new RegistryRef<>();
        ref.bind(object::get, object::getId);
        return ref;
    }

    /** 把收集到的 DeferredRegister 全部挂到 mod 事件总线上。 */
    public void registerAll(IEventBus modEventBus) {
        this.ordered.forEach(deferred -> deferred.register(modEventBus));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private DeferredRegister deferredFor(ResourceKey<?> registry) {
        DeferredRegister deferred = this.registers.get(registry);
        if (deferred == null) {
            deferred = DeferredRegister.create((ResourceKey) registry, Constants.MOD_ID);
            this.registers.put(registry, deferred);
            this.ordered.add(deferred);
        }
        return deferred;
    }
}
