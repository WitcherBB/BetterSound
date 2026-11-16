package com.witcherbb.bettersound.menu;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.client.gui.screen.inventory.BetterJukeboxScreen;
import com.witcherbb.bettersound.client.gui.screen.inventory.ExampleScreen;
import com.witcherbb.bettersound.client.gui.screen.inventory.JukeboxControllerScreen;
import com.witcherbb.bettersound.client.gui.screen.inventory.NoteBlockScreen;
import com.witcherbb.bettersound.menu.inventory.BetterJukeboxMenu;
import com.witcherbb.bettersound.menu.inventory.ExampleMenu;
import com.witcherbb.bettersound.menu.inventory.JukeboxControllerMenu;
import com.witcherbb.bettersound.menu.inventory.NoteBlockMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Constants.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<ExampleMenu>> EXAMPLE_MENU =
            registerMenu("example_menu", ExampleMenu::new);
    public static final RegistrySupplier<MenuType<JukeboxControllerMenu>> JUKEBOX_CONTROLLER_MENU =
            registerMenu("jukebox_controller_menu", JukeboxControllerMenu::new);
    public static final RegistrySupplier<MenuType<NoteBlockMenu>> NOTE_BLOCK_MENU =
            registerMenu("note_block_menu", NoteBlockMenu::new);
    public static final RegistrySupplier<MenuType<BetterJukeboxMenu>> BETTER_JUKEBOX_MENU =
            registerMenu("better_jukebox_menu", BetterJukeboxMenu::new);

    public static void register() {
        MENUS.register();
    }

    public static void registerScreenHandler() {
        MenuRegistry.registerScreenFactory(ModMenuTypes.EXAMPLE_MENU.get(), ExampleScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.JUKEBOX_CONTROLLER_MENU.get(), JukeboxControllerScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.NOTE_BLOCK_MENU.get(), NoteBlockScreen::new);
        MenuRegistry.registerScreenFactory(ModMenuTypes.BETTER_JUKEBOX_MENU.get(), BetterJukeboxScreen::new);
    }

    private static <T extends AbstractContainerMenu> RegistrySupplier<MenuType<T>> registerMenu(String id, MenuRegistry.ExtendedMenuTypeFactory<T> factory) {
        return MENUS.register(id, () -> MenuRegistry.ofExtended(factory));
    }

    private ModMenuTypes() {
    }
}
