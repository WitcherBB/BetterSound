package com.witcherbb.bettersound.fabric.config;

import com.witcherbb.bettersound.Constants;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

public final class FabricCommonConfig {
    public static ConfigClassHandler<FabricCommonConfig> HANDLER = ConfigClassHandler.createBuilder(FabricCommonConfig.class)
            .id(new ResourceLocation(Constants.MOD_ID, "common_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("bettersound/common.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "The maximum number of beats that the sustain pedal can be held down for")
    private int midiMaxSustainBars = 4;

    @SerialEntry(comment = "How often does the pedal default to being released in small sections")
    private double releaseSubsections = 1.0;

    public int getMidiMaxSustainBars() {
        return midiMaxSustainBars;
    }

    public void setMidiMaxSustainBars(int midiMaxSustainBars) {
        this.midiMaxSustainBars = midiMaxSustainBars;
    }

    public double getReleaseSubsections() {
        return releaseSubsections;
    }

    public void setReleaseSubsections(double releaseSubsections) {
        this.releaseSubsections = releaseSubsections;
    }
}
