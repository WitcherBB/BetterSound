package com.witcherbb.bettersound.blocks.state.properties;

public enum PianoPart implements BlockPart{
    /**
     * 按渲染顺序排放
     */
    PEDAL("pedal"),
    PEDAL_R("pedal_right"),
    KEYBOARD_R("keyboard_right"),
    KEYBOARD_M("keyboard_middle"),
    KEYBOARD_L("keyboard_left"),
    PEDAL_L("pedal_left");

    private final String name;

    PianoPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
