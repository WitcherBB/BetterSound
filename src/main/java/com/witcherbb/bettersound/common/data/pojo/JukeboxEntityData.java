package com.witcherbb.bettersound.common.data.pojo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.witcherbb.tool.annotation.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JukeboxEntityData extends BaseEntityData{
	public static final Codec<JukeboxEntityData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
				Codec.STRING.fieldOf("name").forGetter(JukeboxEntityData::getName),
				Codec.STRING.fieldOf("dimension").forGetter(JukeboxEntityData::getDimension),
				BlockPos.CODEC.listOf().fieldOf("controllerPosList").forGetter(JukeboxEntityData::getControllerPosList),
				BlockPos.CODEC.listOf().fieldOf("posList").forGetter(JukeboxEntityData::getPosList)
		).apply(instance, JukeboxEntityData::new)
	);
	private final String name;
	private final String dimension;
	private List<BlockPos> controllerPosList;
	private List<BlockPos> posList;



	public JukeboxEntityData(String name, String dimension) {
		this.name = name;
		this.posList = new ArrayList<>();
		this.controllerPosList = new ArrayList<>();
		this.dimension = dimension;
	}

	private JukeboxEntityData(String name, String dimension, List<BlockPos> controllerPosList, List<BlockPos> posList) {
		this.name = name;
        this.posList = posList;
        this.controllerPosList = controllerPosList;
        this.dimension = dimension;
	}

	public String getName() {
		return name;
	}

	public List<BlockPos> getPosList() {
		return posList;
	}

	public void setPosList(List<BlockPos> posList) {
		this.posList = posList;
	}

	public List<BlockPos> getControllerPosList() {
		return controllerPosList;
	}

	public void setControllerPosList(List<BlockPos> controllerPosList) {
		this.controllerPosList = controllerPosList;
	}

	public String getDimension() {
		return dimension;
	}

	public void addBlockPos(BlockPos pos) {
		this.posList.add(pos);
	}

	public void removeBlockPos(BlockPos pos) {
		this.posList.remove(pos);
	}

	public void addControllerPos(BlockPos pos) {
		this.controllerPosList.add(pos);
	}

	public void removeControllerPos(BlockPos pos) {
		this.controllerPosList.remove(pos);
	}

	public boolean same(String name, String dimension) {
		return this.name.equals(name) && this.dimension.equals(dimension);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JukeboxEntityData that = (JukeboxEntityData) o;
		return Objects.equals(name, that.name) && Objects.equals(dimension, that.dimension);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, dimension, controllerPosList, posList);
	}
}
