package com.witcherbb.bettersound.world.structure;

import java.util.HashMap;
import java.util.Map;

public class NbtPieceBuilder {
    private Map<String, Integer> structureAndWeight = new HashMap<>();

    public Map<String, Integer> build() {
        return this.structureAndWeight;
    }

    public NbtPieceBuilder add(String nbtPieceURL, Integer weight) {
        structureAndWeight.put(nbtPieceURL, weight);
        return this;
    }
}
