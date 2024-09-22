package com.witcherbb.bettersound.common.data;

import com.witcherbb.bettersound.common.ModFile;
import com.witcherbb.bettersound.common.data.pojo.BaseEntityData;
import com.witcherbb.bettersound.common.data.pojo.JukeboxEntityData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class ModDataProvider<T extends BaseEntityData> extends ModFile<ModDataProvider.Data<T>> {
	protected Data<T> data;

	public ModDataProvider(String fileName, Type type) {
		super(fileName, type);
		this.data = new Data<>();
	}

	public void updateToFile() {
		writeData();
	}

	/**
	 * 需要用到返回值可以重写该方法
	 * @param t 指定的数据对象
	 */
	public void addData(T t) {
		this.data.addData(t);
	}

	/**
	 * 需要用到返回值可以重写该方法
	 * @param t 指定的数据对象
	 */
	public void removeData(T t) {
		this.data.removeData(t);
	}

	public boolean exists(T t) {
		List<T> dataList = this.data.getDataList();
		int size = dataList.size();
		for (int i = 0; i < size; i++) {
			if (dataList.get(i).equals(t)) {
				return true;
			}
		}
		return false;
	}

	public void init(String levelName) {
		create(levelName, this);
		readData();
	}

	private void writeData() {
		this.wirte2json(this.data);
	}

	private void readData() {
		this.data = this.readFromJson();
		if (this.data.getDataList() == null) this.data = new Data<>();
	}

	public List<T> getData() {
		return data.getDataList();
	}

	public void setData(List<T> set) {
		this.data.setDataList(set);
	}

	public abstract String toString(String name);

	protected static class Data<T> {
		//data作为所有独立数据的数据的数组 {"data": [{"name": "name1", ...}, {"name": "name2", ...}, ...]}
		private List<T> dataList;

		protected Data() {
			this(new ArrayList<>());
		}

		protected Data(List<T> dataList) {
			this.dataList = dataList;
		}

		protected List<T> getDataList() {
			return dataList;
		}

		protected void setDataList(List<T> dataList) {
			this.dataList = dataList;
		}

		protected boolean addData(T t) {
			return this.dataList.add(t);
		}

		protected boolean removeData(T t) {
			return this.dataList.remove(t);
		}
	}

}
