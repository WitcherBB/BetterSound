package com.witcherbb.bettersound.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public abstract class ModFile<T> {
	protected static final Logger LOGGER = LogUtils.getLogger();
	protected static String gen = "data/BetterSound";
	protected File file;
	protected final String fileName;
	protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Type type;

	public ModFile(String name, Type type) {
		this.fileName = name;
		this.type = type;
	}

	public static <T> void create(String levelName, ModFile<T> modFile) {
		File newFile = new File("%s\\%s".formatted(gen, levelName), "%s.json".formatted(modFile.getFileName()));
		File parentDir = new File("%s\\%s".formatted(gen, levelName));
		try {
			if (!parentDir.exists()) parentDir.mkdirs();
			if (newFile.exists()){
				Reader reader = new InputStreamReader(new FileInputStream(newFile), StandardCharsets.UTF_8);
				StringBuilder str = new StringBuilder();
				int c;
				while((c = reader.read()) != -1) {
					str.append((char) c);
				}
				reader.close();
				if (str.toString().isEmpty()) {
					Writer writer = new FileWriter(newFile, false);
					writer.write("{}");
					writer.close();
				}
				modFile.file = newFile;
				return;
			}
			newFile.createNewFile();
			Writer writer = new FileWriter(newFile, false);
			writer.write("{}");
			writer.close();
			modFile.file = newFile;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	protected void wirte2json(T t) {
		String json = gson.toJson(t);
		try {
			Writer writer = new FileWriter(file, false);
			writer.write(json);
			writer.close();
		} catch (Exception e) {
			System.err.printf("writeToJson wrong: %s\n%s\n", e.getMessage(), json);
		}
	}

	protected T readFromJson() {
		try {
			Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
			T data = gson.fromJson(reader, this.type);
			reader.close();
			return data;
		} catch (Exception e) {
			throw new IllegalStateException("readFromJson wrong");
		}
	}

	public String getFileName() {
		return fileName;
	}
}
