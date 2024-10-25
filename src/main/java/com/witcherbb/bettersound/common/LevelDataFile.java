package com.witcherbb.bettersound.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Optional;

public class LevelDataFile<T> {
	protected static final Logger LOGGER = LogUtils.getLogger();
	protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	protected static File worldPath;
	protected File dataFile;
	protected final String fileName;
	protected Codec<T> codec;
	private final Type type;

	public LevelDataFile(String name, Type type, Codec<T> codec) {
		this.fileName = name;
		this.type = type;
		this.codec = codec;
	}

	public static <T> void create(String levelName, LevelDataFile<T> levelDataFile) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		Path serverPath = Path.of(server.getServerDirectory().getPath());
		if (server instanceof IntegratedServer) {
			worldPath = serverPath.resolve("saves").resolve(levelName + "/data").toFile();
		} else {
			worldPath = serverPath.resolve(levelName + "/data").toFile();
		}

		File newFile = new File(worldPath, levelDataFile.getFileName() + ".nbt");
		worldPath.mkdirs();
		if (newFile.exists()) {
			// 空json文件添加"{}"
//				Reader reader = new InputStreamReader(new FileInputStream(newFile), StandardCharsets.UTF_8);
//				StringBuilder str = new StringBuilder();
//				int c;
//				while((c = reader.read()) != -1) {
//					str.append((char) c);
//				}
//				reader.close();
//				if (str.toString().isEmpty()) {
//					Writer writer = new FileWriter(newFile, false);
//					writer.write("{}");
//					writer.close();
//				}
			levelDataFile.dataFile = newFile;
			return;
		}
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 空json文件添加"{}"
//			Writer writer = new FileWriter(newFile, false);
//			writer.write("{}");
//			writer.close();
		levelDataFile.dataFile = newFile;
	}

	public void wirteToNbt(T t) {
		String json = gson.toJson(t);
		try {
			CompoundTag tag = new CompoundTag();
			tag.put("Targets", codec.encodeStart(NbtOps.INSTANCE, t).result().orElse(new CompoundTag()));
			NbtIo.write(tag, dataFile);
		} catch (Exception e) {
			System.err.printf("writeToJson wrong: %s\n%s\n", e.getMessage(), json);
		}
	}

	public Optional<T> readFromJson() {
		try {
			CompoundTag tag = NbtIo.read(dataFile);
            return codec.parse(NbtOps.INSTANCE, tag.get("Targets")).result();
		} catch (Exception e) {
			throw new IllegalStateException("readFromJson wrong");
		}
	}

	public String getFileName() {
		return fileName;
	}
}
