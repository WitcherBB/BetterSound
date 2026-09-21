package com.witcherbb.bettersound.menu;

import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import com.witcherbb.bettersound.menu.inventory.JukeboxMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Forge 专属的菜单登记：原版唱片机菜单用的物品能力（{@code ForgeCapabilities}）是 Forge 独有的，
 * 所以这一条留在 forge 侧。
 */
public final class ForgeMenuTypes {

    public static RegistryRef<MenuType<JukeboxMenu>> JUKEBOX_MENU;

    public static void register(ModRegistrar registrar) {
        JUKEBOX_MENU = registrar.registerMenu("jukebox_menu", JukeboxMenu::new);
    }

    private ForgeMenuTypes() {
    }
}
