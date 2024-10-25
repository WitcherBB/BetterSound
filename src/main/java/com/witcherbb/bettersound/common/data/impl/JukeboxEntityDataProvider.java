package com.witcherbb.bettersound.common.data.impl;

import com.google.gson.reflect.TypeToken;
import com.witcherbb.bettersound.common.data.ListDataProvider;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JukeboxEntityDataProvider extends ListDataProvider<JukeboxEntityData> {

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
		List<JukeboxEntityData> dataList = this.data;
		int size = dataList.size();
		for (int i = 0; i < size; i++) {
			JukeboxEntityData data1 = dataList.get(i);
			if (data1.getDimension().equals(data.getDimension()) && data1.getName().equals(data.getName())) {
				return true;
			}
		}

		return false;
	}

	public boolean exists(String name, String dimension) {
		JukeboxEntityData[] dataArray = this.data.toArray(new JukeboxEntityData[0]);
		boolean exsists = false;
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				exsists = true;
			}
		}
		return exsists;
	}

	public void addPos(String name, String dimension, BlockPos pos) {
		if (!exists(name, dimension)) return;
		this.addPosListByNameAndDimension(name, dimension, pos);
	}

	public void addControllerPos(String name, String dimension, BlockPos pos) {
		JukeboxEntityData[] dataArray = this.data.toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				dataArray[i].addControllerPos(pos);
				return;
			}
		}
	}

	public boolean removeControllerPos(String name, String dimension, BlockPos pos) {
		List<JukeboxEntityData> dataList = this.data;
		List<BlockPos> controllerPoses = List.of();
		JukeboxEntityData foundData = null;

		for (int i = 0; i < dataList.size(); i++) {
			JukeboxEntityData data1 = dataList.get(i);
			if (data1.same(name, dimension)) {
				foundData = data1;
				controllerPoses = foundData.getControllerPosList();
				break;
			}
		}
		// foundData 和 controllerPoses 同时改变， 所以只判断一个即可
		if (Objects.isNull(foundData) || controllerPoses.isEmpty()) return false;
		for (int i = 0; i < controllerPoses.size(); i++) {
			BlockPos controllerPos = controllerPoses.get(i);
			if (controllerPos.equals(pos)) {
				controllerPoses.remove(controllerPos);
				if (controllerPoses.isEmpty()) {
					this.removeData(foundData);
					return true;
				}
			}
		}
		return false;
	}

	public List<BlockPos> getPosListByNameAndDimension(String name, String dimension) {
		JukeboxEntityData[] dataArray = this.data.toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				return dataArray[i].getPosList();
			}
		}
		return new ArrayList<>();
	}

	public List<BlockPos> getControllerPosListByNameAndDimension(String name, String dimension) {
		JukeboxEntityData[] dataArray = this.data.toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				return dataArray[i].getControllerPosList();
			}
		}
		return new ArrayList<>();
	}

	public void addPosListByNameAndDimension(String name, String dimension, BlockPos pos) {
		JukeboxEntityData[] dataArray = this.data.toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				dataArray[i].addBlockPos(pos);
			}
		}
	}
}
