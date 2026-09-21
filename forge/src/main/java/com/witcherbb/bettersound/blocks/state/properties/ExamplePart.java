package com.witcherbb.bettersound.blocks.state.properties;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ExamplePart implements BlockPart {
    FIRST("first"),
    SECOND("second");

    private final String name;

    ExamplePart(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name;
    }
}
