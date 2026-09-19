package com.witcherbb.bettersound;

import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = BetterSound.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
    static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {

        /** MIDI 延音踏板最长保持多少个小节，超过则强制释放一次；0 表示关闭该保护 */
        public final ForgeConfigSpec.IntValue midiMaxSustainBars;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Common configuration settings")
                    .push("common");
            builder.define("version", "0.0.0.0-demo");
            builder.pop();

            builder.comment("MIDI playback settings")
                    .push("midi");
            midiMaxSustainBars = builder
                    .comment("MIDI 延音踏板最长保持的小节数。",
                            "超过该长度仍未抬起踏板时会强制释放一次，避免曲子里踏板一直踩住导致声音糊成一片。",
                            "0 = 关闭该保护，完全跟随 MIDI 记录。")
                    .defineInRange("maxSustainBars", 4, 0, 64);
            builder.pop();
        }
    }
}
