package com.witcherbb.bettersound.common.init;

import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;

public class DataManager {
	public static void init() {
		JukeboxControllerBlockEntity.getProvider().init(BetterSound.currentLevelName);
	}
	public static void save() {
		JukeboxControllerBlockEntity.getProvider().updateToFile();
	}
}
