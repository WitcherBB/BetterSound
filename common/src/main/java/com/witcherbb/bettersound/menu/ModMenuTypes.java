package com.witcherbb.bettersound.menu;

import com.witcherbb.bettersound.common.registry.ModRegistrar;
import com.witcherbb.bettersound.common.registry.RegistryRef;
import com.witcherbb.bettersound.menu.inventory.ExampleMenu;
import com.witcherbb.bettersound.menu.inventory.JukeboxControllerMenu;
import com.witcherbb.bettersound.menu.inventory.NoteBlockMenu;
import com.witcherbb.bettersound.menu.inventory.PianoBlockMenu;
import com.witcherbb.bettersound.menu.inventory.ToneBlockMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * 菜单类型登记表。
 *
 * <p>原版唱片机那个菜单（{@code JukeboxMenu}）用了 Forge 的物品能力，
 * 它的登记留在 forge 侧的 {@code ForgeMenuTypes} 里。
 */
public final class ModMenuTypes {

    public static RegistryRef<MenuType<ExampleMenu>> EXAMPLE_MENU;
    public static RegistryRef<MenuType<JukeboxControllerMenu>> JUKEBOX_CONTROLLER_MENU;
    public static RegistryRef<MenuType<NoteBlockMenu>> NOTE_BLOCK_MENU;
    public static RegistryRef<MenuType<PianoBlockMenu>> PIANO_BLOCK_MENU;
    public static RegistryRef<MenuType<ToneBlockMenu>> TONE_BLOCK_MENU;

    public static void register(ModRegistrar registrar) {
        EXAMPLE_MENU = registrar.registerMenu("example_menu", ExampleMenu::new);
        JUKEBOX_CONTROLLER_MENU = registrar.registerMenu("jukebox_controller_menu", JukeboxControllerMenu::new);
        NOTE_BLOCK_MENU = registrar.registerMenu("note_block_menu", NoteBlockMenu::new);
        PIANO_BLOCK_MENU = registrar.registerMenu("piano_block_menu", PianoBlockMenu::new);
        TONE_BLOCK_MENU = registrar.registerMenu("tone_block_menu", ToneBlockMenu::new);
    }

    private ModMenuTypes() {
    }
}
