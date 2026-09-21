package com.witcherbb.bettersound.menu;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.menu.inventory.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BetterSound.MODID);

	public static final RegistryObject<MenuType<JukeboxMenu>> JUKEBOX_MENU =
			registerMenuType("jukebox_menu", JukeboxMenu::new);
	public static final RegistryObject<MenuType<ExampleMenu>> EXAMPLE_MENU =
			registerMenuType("example_menu", ExampleMenu::new);
	public static final RegistryObject<MenuType<JukeboxControllerMenu>> JUKEBOX_CONTROLLER_MENU =
			registerMenuType("jukebox_controller_menu", JukeboxControllerMenu::new);
	public static final RegistryObject<MenuType<NoteBlockMenu>> NOTE_BLOCK_MENU =
			registerMenuType("note_block_menu", NoteBlockMenu::new);
	public static final RegistryObject<MenuType<PianoBlockMenu>> PIANO_BLOCK_MENU =
			registerMenuType("piano_block_menu", PianoBlockMenu::new);
	public static final RegistryObject<MenuType<ToneBlockMenu>> TONE_BLOCK_MENU =
			registerMenuType("tone_block_menu", ToneBlockMenu::new);

	private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
		return MENUS.register(name, () -> IForgeMenuType.create(factory));
	}
}
