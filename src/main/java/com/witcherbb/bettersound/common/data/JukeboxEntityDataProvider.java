package com.witcherbb.bettersound.common.data;

import com.google.gson.reflect.TypeToken;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class JukeboxEntityDataProvider extends ModDataProvider<JukeboxEntityData> {

	public JukeboxEntityDataProvider() {
		super("jukebox_data", new TypeToken<ModDataProvider.Data<JukeboxEntityData>>(){}.getType());
	}

	public void update(String posDataString, String name) {
		List<JukeboxEntityData.Pos> list = parse(posDataString);
		JukeboxEntityData[] array = data.getDataList().toArray(new JukeboxEntityData[0]);
		int size = array.length;
		for (int i = 0; i < size; i++) {
			if (array[i].getName().equals(name)) {
				array[i].setPosList(list);
				return;
			}
		}
	}

	@Override
	public void addData(JukeboxEntityData jukeboxEntityData) {
		if (exists(jukeboxEntityData.getName(), jukeboxEntityData.getDimension())) return;
		super.addData(jukeboxEntityData);
	}

	public void addData(JukeboxEntityData jukeboxEntityData, JukeboxEntityData.Pos controllerPos) {
		this.addData(jukeboxEntityData);
		this.addControllerPos(jukeboxEntityData.getName(), jukeboxEntityData.getDimension(), controllerPos);
	}

	public boolean exists(String name, String dimension) {
		JukeboxEntityData[] dataArray = this.data.getDataList().toArray(new JukeboxEntityData[0]);
		boolean exsists = false;
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				exsists = true;
			}
		}
		return exsists;
	}

	@Override
	public boolean exists(JukeboxEntityData entityData) {
		List<JukeboxEntityData> dataList = this.data.getDataList();
		int size = dataList.size();
		for (int i = 0; i < size; i++) {
			JukeboxEntityData data1 = dataList.get(i);
			if (data1.getDimension().equals(entityData.getDimension()) && data1.getName().equals(entityData.getName())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString(String name) {
		StringBuffer str = new StringBuffer("[");
		JukeboxEntityData[] dataArray = getData().toArray(new JukeboxEntityData[0]);
		int size1 = dataArray.length;
		for (int i = 0; i < size1; i++) {
			if (dataArray[i].getName().equals(name)) {
				List<JukeboxEntityData.Pos> list = dataArray[i].getPosList();
				for (int j = 0; j < list.size(); j++) {
					JukeboxEntityData.Pos pos = list.get(j);
					String sub = pos.toString();
					if (j < list.size() - 1) sub += ", ";
					str.append(sub);
				}
				break;
			}
		}
		str.append("]");
		return str.toString();
	}

	public void addPos(String name, String dimension, JukeboxEntityData.Pos pos) {
		if (!exists(name, dimension)) return;
		this.addPosListByNameAndDimension(name, dimension, pos);
	}

	public void removePos(String name, String dimension, JukeboxEntityData.Pos pos) {
		JukeboxEntityData[] dataArray = this.data.getDataList().toArray(new JukeboxEntityData[0]);
		List<JukeboxEntityData.Pos> posList = null;
		JukeboxEntityData foundData =  null;
		int size1 = dataArray.length;
		for (int i = 0; i < size1; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				posList = dataArray[i].getPosList();
				foundData = dataArray[i];
				break;
			}
		}
		if (posList == null || foundData == null) return;
		JukeboxEntityData.Pos[] posArray = posList.toArray(new JukeboxEntityData.Pos[0]);
		int size2 = posArray.length;
		for (int i = 0; i < size2; i++) {
			JukeboxEntityData.Pos pPos = posArray[i];
			if (pos.equals(pPos)) {
				foundData.removeBlockPos(pPos);
				break;
			}
		}
	}

	public void addControllerPos(String name, String dimension, JukeboxEntityData.Pos pos) {
		JukeboxEntityData[] dataArray = this.data.getDataList().toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				dataArray[i].addControllerPos(pos);
				return;
			}
		}
	}

	public boolean removeControllerPos(String name, String dimension, JukeboxEntityData.Pos pos) {
		JukeboxEntityData[] dataArray = this.data.getDataList().toArray(new JukeboxEntityData[0]);
		List<JukeboxEntityData.Pos> posList = null;
		JukeboxEntityData foundData =  null;
		int size1 = dataArray.length;
		for (int i = 0; i < size1; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				foundData = dataArray[i];
				posList = dataArray[i].getControllerPosList();
				break;
			}
		}
		if (posList == null || foundData == null) return false;
		JukeboxEntityData.Pos[] posArray = posList.toArray(new JukeboxEntityData.Pos[0]);
		int size2 = posArray.length;
		for (int i = 0; i < size2; i++) {
			JukeboxEntityData.Pos pPos = posArray[i];
			if (pPos.equals(pos)) {
				foundData.removeControllerPos(pPos);
				if (posList.isEmpty()) {
					this.removeData(foundData);
					return true;
				}
			}
		}
		return false;
	}

	public List<JukeboxEntityData.Pos> getPosListByNameAndDimension(String name, String dimension) {
		JukeboxEntityData[] dataArray = this.data.getDataList().toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				return dataArray[i].getPosList();
			}
		}
		return new ArrayList<>();
	}

	public List<JukeboxEntityData.Pos> getControllerPosListByNameAndDimension(String name, String dimension) {
		JukeboxEntityData[] dataArray = this.data.getDataList().toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				return dataArray[i].getControllerPosList();
			}
		}
		return new ArrayList<>();
	}

	public void addPosListByNameAndDimension(String name, String dimension, JukeboxEntityData.Pos pos) {
		JukeboxEntityData[] dataArray = this.data.getDataList().toArray(new JukeboxEntityData[0]);
		int size = dataArray.length;
		for (int i = 0; i < size; i++) {
			if (dataArray[i].getName().equals(name) && dataArray[i].getDimension().equals(dimension)) {
				dataArray[i].addBlockPos(pos);
			}
		}
	}

	private List<JukeboxEntityData.Pos> parse(String posDataString) {
		posDataString = StringUtils.strip(posDataString, "[]");
		if (posDataString.isEmpty()) return new ArrayList<>();
		String[] posStrings = posDataString.split(",");
		List<JukeboxEntityData.Pos> list = new ArrayList<>();
		int size = posStrings.length;
		for (int i = 0; i < size; i++) {
			JukeboxEntityData.Pos pos = JukeboxEntityData.Pos.parse(posStrings[i]);
			if (pos == null) return new ArrayList<>();
			list.add(pos);
		}
		return list;
	}
}
