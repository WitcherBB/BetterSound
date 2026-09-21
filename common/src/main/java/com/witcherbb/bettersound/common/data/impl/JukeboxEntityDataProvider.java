package com.witcherbb.bettersound.common.data.impl;

import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import com.witcherbb.bettersound.common.data.AbstractJukeboxDataProvider;
import com.witcherbb.bettersound.common.data.ListDataProvider;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JukeboxEntityDataProvider extends AbstractJukeboxDataProvider {

	public JukeboxEntityDataProvider() {
		super("jukebox_data", new TypeToken<List<JukeboxEntityData>>(){}.getType(), JukeboxEntityData.CODEC);
	}

	@Override
	public void addData(JukeboxEntityData data) {
		if (this.exists(data.getName(), data.getDimension())) return;
		super.addData(data);
	}

	public void addData(JukeboxEntityData data, BlockPos controllerPos) {
		this.addData(data);
		this.addControllerPos(data.getName(), data.getDimension(), controllerPos);
	}

	@Override
	public boolean exists(JukeboxEntityData data) {
        return exists(data.getName(), data.getDimension());
    }

	public boolean exists(String name, String dimension) {
		Set<Pair<String, String>> keySet = this.data.keySet();
		return keySet.contains(new Pair<>(name, dimension));
	}

	public void addPos(String name, String dimension, BlockPos pos) {
		if (!exists(name, dimension)) return;
		this.addPosListByNameAndDimension(name, dimension, pos);
	}

	public void addControllerPos(String name, String dimension, BlockPos pos) {
		var key = new Pair<>(name, dimension);
		if (!this.data.containsKey(key)) return;
		var jd = this.data.get(key);
		if (jd.getControllerPosList().contains(pos)) return;
		jd.addControllerPos(pos);
	}

	public boolean removeControllerPos(String name, String dimension, BlockPos pos) {
		var key = new Pair<>(name, dimension);
		List<BlockPos> controllerPoses = List.of();
		JukeboxEntityData foundData = this.data.get(key);
		if (!Objects.isNull(foundData)) {
			controllerPoses = foundData.getControllerPosList();
		}

		if (Objects.isNull(foundData) || controllerPoses.isEmpty()) return false;
		controllerPoses.remove(pos);
		if (controllerPoses.isEmpty()) {
			this.removeData(foundData);
			return true;
		}

		return false;
	}

	public List<BlockPos> getPosListByNameAndDimension(String name, String dimension) {
		var key = new Pair<>(name, dimension);
		if (this.data.containsKey(key)) {
			return this.data.get(key).getPosList();
		}
		return new ArrayList<>();
	}

	public List<BlockPos> getControllerPosListByNameAndDimension(String name, String dimension) {
		var key = new Pair<>(name, dimension);
		if (this.data.containsKey(key)) {
			return this.data.get(key).getControllerPosList();
		}
		return new ArrayList<>();
	}

	public void addPosListByNameAndDimension(String name, String dimension, BlockPos pos) {
		var key = new Pair<>(name, dimension);
		if (this.data.containsKey(key)) {
			this.data.get(key).addBlockPos(pos);
		}
	}
}
