package com.witcherbb.bettersound.common.init;

import com.witcherbb.bettersound.blocks.entity.JukeboxControllerBlockEntity;

public class DataManager {
	public static void init(String levelName) {
		JukeboxControllerBlockEntity.getProvider().init(levelName);
	}
	public static void save() {
		JukeboxControllerBlockEntity.getProvider().updateToFile();
	}
}
