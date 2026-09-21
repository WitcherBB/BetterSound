package com.witcherbb.bettersound.common.data;

import com.mojang.serialization.Codec;
import com.witcherbb.bettersound.common.LevelDataFile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class ModDataProvider<D> {
    protected final LevelDataFile<D> dataFile;
    private final Supplier<D> defaultSupplier;

    protected ModDataProvider(String fileName, Type type, Codec<D> codec, Supplier<D> defaultSupplier) {
        this.dataFile = new LevelDataFile<>(fileName, type, codec);
        this.defaultSupplier = defaultSupplier;
    }

    public void updateToFile() {
        this.dataFile.wirteToNbt(this.getData());
    }

    public void init(String levelName) {
        LevelDataFile.create(levelName, this.dataFile);
        readData();
    }

    private void readData() {
        this.setData(this.dataFile.readFromJson().orElse(this.defaultSupplier.get()));
    }

    public abstract D getData();

    public abstract void setData(D d);
}
