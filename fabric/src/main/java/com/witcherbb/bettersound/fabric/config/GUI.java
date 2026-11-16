package com.witcherbb.bettersound.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class GUI implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> {
            FabricClientConfig clientConfig = FabricClientConfig.HANDLER.instance();
            FabricCommonConfig commonConfig = FabricCommonConfig.HANDLER.instance();

            return YetAnotherConfigLib.createBuilder()
                    .title(Component.literal("BetterSound Configuration"))
                    .save(() -> {
                        FabricClientConfig.HANDLER.save();
                        FabricCommonConfig.HANDLER.save();
                    })
                    .category(ConfigCategory.createBuilder()
                            .name(Component.literal("Client Config"))
                            .option(Option.<Boolean>createBuilder()
                                    .name(Component.literal("Show Tone"))
                                    .description(OptionDescription.of(Component.literal("Does the Tone Block display the scale at the top")))
                                    .binding(true, clientConfig::isShowTone, clientConfig::setShowTone)
                                    .controller(TickBoxControllerBuilder::create)
                                    .build())
                            .build())
                    .category(ConfigCategory.createBuilder()
                            .name(Component.literal("Common Config"))
                            .group(OptionGroup.createBuilder()
                                    .name(Component.literal("AutoPlay"))
                                    .option(Option.<Integer>createBuilder()
                                            .name(Component.literal("Max Sustain Bars"))
                                            .description(OptionDescription.of(Component.literal("The maximum number of beats that the sustain pedal can be held down for")))
                                            .binding(4, commonConfig::getMidiMaxSustainBars, commonConfig::setMidiMaxSustainBars)
                                            .controller(option -> IntegerSliderControllerBuilder.create(option)
                                                    .range(0, 64).step(1))
                                            .build())
                                    .option(Option.<Double>createBuilder()
                                            .name(Component.literal("Release Subsections"))
                                            .description(OptionDescription.of(Component.literal("How often does the pedal default to being released in small sections")))
                                            .binding(1.0, commonConfig::getReleaseSubsections, commonConfig::setReleaseSubsections)
                                            .controller(option -> DoubleSliderControllerBuilder.create(option)
                                                    .range(0.0, 4.0).step(0.1))
                                            .build())
                                    .build())
                            .build())
                    .build().generateScreen(parentScreen);
        };
    }
}
