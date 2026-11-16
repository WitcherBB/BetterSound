package com.witcherbb.bettersound.fabric.config;

import com.witcherbb.bettersound.Constants;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class FabricClientConfig {
    public static ConfigClassHandler<FabricClientConfig> HANDLER = ConfigClassHandler.createBuilder(FabricClientConfig.class)
            .id(new ResourceLocation(Constants.MOD_ID, "client_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("bettersound/client.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Does the Tone Block display the scale at the top")
    private boolean showTone = true;

    public boolean isShowTone() {
        return showTone;
    }

    public void setShowTone(boolean showTone) {
        this.showTone = showTone;
    }


}
