package com.witcherbb.bettersound.common.data.pojo;

import com.witcherbb.bettersound.BetterSound;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JukeboxEntityData extends BaseEntityData{
	private String name;
	private String dimension;
	private List<Pos> controllerPosList;
	private List<Pos> posList;


	private static final String[] AXIS = new String[]{"\"x\":", "\"y\":", "\"z\":"};

	public JukeboxEntityData(String name, String dimension) {
		this.name = name;
		this.posList = new ArrayList<>();
		this.controllerPosList = new ArrayList<>();
		this.dimension = dimension;
	}

	public static Pos createPos(BlockPos blockPos) {
		return new Pos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Pos> getPosList() {
		return posList;
	}

	public void setPosList(List<Pos> posList) {
		this.posList = posList;
	}

	public List<Pos> getControllerPosList() {
		return controllerPosList;
	}

	public void setControllerPosList(List<Pos> controllerPosList) {
		this.controllerPosList = controllerPosList;
	}

	public String getDimension() {
		return dimension;
	}

	public boolean addBlockPos(Pos pos) {
		return this.posList.add(pos);
	}

	public boolean removeBlockPos(Pos pos) {
		return this.posList.remove(pos);
	}

	public boolean addControllerPos(Pos pos) {
		return this.controllerPosList.add(pos);
	}

	public boolean removeControllerPos(Pos pos) {
		return this.controllerPosList.remove(pos);
	}

	public static JukeboxBlockEntity getBlockEntity(Level level, Pos pos) {
		BlockEntity blockEntity = level.getBlockEntity(new BlockPos(pos.x, pos.y, pos.z));
		if (blockEntity instanceof JukeboxBlockEntity jukeboxBlockEntity)
			return jukeboxBlockEntity;
		return null;
	}

    public record Pos(int x, int y, int z) {
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Pos pos) {
				return (pos.x == this.x) && (pos.y == this.y) && (pos.z == this.z);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y, z);
		}

		public BlockPos toBlockPos() {
			return new BlockPos(this.x, this.y, this.z);
		}

		@Override
		public String toString() {
			String module = "{\"x\":%d, \"y\":%d, \"z\":%d}";
			return module.formatted(x, y, z);
		}

		@Nullable
		public static Pos parse(String s) {
			s = StringUtils.strip(s, "{}");
			String[] axisArray = s.split(",");
			Integer[] posValues = new Integer[3];
			if (axisArray.length != AXIS.length) {
				BetterSound.LOGGER.error("parse wrong: length wrong");
				return null;
			}
			for (int i = 0; i < AXIS.length; i++) {
				if (!axisArray[i].contains(AXIS[i])) {
					BetterSound.LOGGER.error("parse wrong: content wrong");
					return null;
				}
				try {
					posValues[i] = Integer.valueOf(axisArray[i].split(AXIS[i])[1]);
				} catch (Exception e) {
					BetterSound.LOGGER.error("parse wrong: content wrong");
					return null;
				}
			}
			return new Pos(posValues[0], posValues[1], posValues[2]);
		}
	}
}
