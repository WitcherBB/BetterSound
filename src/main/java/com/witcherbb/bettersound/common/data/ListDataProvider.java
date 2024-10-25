package com.witcherbb.bettersound.common.data;

import com.mojang.serialization.Codec;
import com.witcherbb.bettersound.common.data.pojo.BaseEntityData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class ListDataProvider<T extends BaseEntityData> extends ModDataProvider<List<T>> {
	protected List<T> data;

	public ListDataProvider(String fileName, Type type, Codec<T> codec) {
		super(fileName, type, codec.listOf(), ArrayList::new);
		this.data = new ArrayList<>();
	}

	@Override
	public List<T> getData() {
		return this.data;
	}

	@Override
	public void setData(List<T> list) {
		this.data = list;
	}

	/**
	 * 需要用到返回值可以重写该方法
	 * @param t 指定的数据对象
	 */
	public void addData(T t) {
		this.data.add(t);
	}

	/**
	 * 需要用到返回值可以重写该方法
	 * @param t 指定的数据对象
	 */
	public void removeData(T t) {
		this.data.remove(t);
	}

	public boolean exists(T t) {
		List<T> dataList = this.data;
		int size = dataList.size();
		for (int i = 0; i < size; i++) {
			if (dataList.get(i).equals(t)) {
				return true;
			}
		}
		return false;
	}
}
