package com.witcherbb.bettersound;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientConfig {
    private static final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
    static final ForgeConfigSpec clientSpec;
    public static Client CLIENT;

    static {
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue showTone;

        Client(ForgeConfigSpec.Builder builder) {
            showTone = builder.comment("音阶方块顶部是否显示音阶")
                .define("显示音阶", true);
        }
    }
}
