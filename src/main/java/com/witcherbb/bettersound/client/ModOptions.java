package com.witcherbb.bettersound.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.client.gui.screen.inventory.AbstractPianoScreen;
import net.minecraft.client.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class ModOptions {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Gson GSON = new Gson();
    public static final File genPath;
    private static final ModOptions instance;
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    private final File optionsFile;

    private static final Integer[] BLACK_KEYS;
    private static final Integer[] WHITE_KEYS;

    public final Map<Lazy<KeyMapping>, Integer> keys = new HashMap<>();
    public Lazy<KeyMapping> keyPianoSustainPedal = Lazy.of(() ->
            new KeyMapping(
                    "key.bettersound.piano_pedal",
                    KeyConflictContext.GUI,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_SPACE,
                    "key.categories.bettersound.keyboard"
            ));

    public ModOptions() {
        this.optionsFile = new File(genPath, "options.txt");
        if (!this.optionsFile.exists()) {
            try {
                this.optionsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        int whiteCount = 0;
        int blackCount = 0;
        for (int i = 27; i < 66; i++) {
            try {
                int key = AbstractPianoScreen.blacks().contains(i) ? BLACK_KEYS[blackCount++] : WHITE_KEYS[whiteCount++];
                keys.put(Lazy.of(() ->
                        new KeyMapping(
                                "key.bettersound.keyboard.key",
                                KeyConflictContext.GUI,
                                InputConstants.Type.KEYSYM,
                                key,
                                "key.categories.bettersound.keyboard"
                        )), i);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

    public static ModOptions getOptions() {
        return instance;
    }

    public void load() {
        try {
            if (!this.optionsFile.exists()) {
                return;
            }

            CompoundTag compoundtag = new CompoundTag();

            try (BufferedReader bufferedreader = Files.newReader(this.optionsFile, Charsets.UTF_8)) {
                bufferedreader.lines().forEach((p_231896_) -> {
                    try {
                        Iterator<String> iterator = OPTION_SPLITTER.split(p_231896_).iterator();
                        compoundtag.putString(iterator.next(), iterator.next());
                    } catch (Exception exception1) {
                        LOGGER.warn("Skipping bad option: {}", (Object)p_231896_);
                    }
                });
            }

            Consumer<Options.FieldAccess> processor = this::processOptions;
            processor.accept(new Options.FieldAccess() {
                @Nullable
                private String getValueOrNull(String p_168459_) {
                    return compoundtag.contains(p_168459_) ? compoundtag.getString(p_168459_) : null;
                }

                public <T> void process(String p_232125_, OptionInstance<T> p_232126_) {
                    String s = this.getValueOrNull(p_232125_);
                    if (s != null) {
                        JsonReader jsonreader = new JsonReader(new StringReader(s.isEmpty() ? "\"\"" : s));
                        JsonElement jsonelement = JsonParser.parseReader(jsonreader);
                        DataResult<T> dataresult = p_232126_.codec().parse(JsonOps.INSTANCE, jsonelement);
                        dataresult.error().ifPresent((p_232130_) -> {
                            LOGGER.error("Error parsing option value {} for option {}: {}", s, p_232126_, p_232130_.message());
                        });
                        dataresult.result().ifPresent(p_232126_::set);
                    }

                }

                public int process(String p_168467_, int p_168468_) {
                    String s = this.getValueOrNull(p_168467_);
                    if (s != null) {
                        try {
                            return Integer.parseInt(s);
                        } catch (NumberFormatException numberformatexception) {
                            LOGGER.warn("Invalid integer value for option {} = {}", p_168467_, s, numberformatexception);
                        }
                    }

                    return p_168468_;
                }

                public boolean process(String p_168483_, boolean p_168484_) {
                    String s = this.getValueOrNull(p_168483_);
                    return s != null ? isTrue(s) : p_168484_;
                }

                public String process(String p_168480_, String p_168481_) {
                    return MoreObjects.firstNonNull(this.getValueOrNull(p_168480_), p_168481_);
                }

                public float process(String p_168464_, float p_168465_) {
                    String s = this.getValueOrNull(p_168464_);
                    if (s != null) {
                        if (isTrue(s)) {
                            return 1.0F;
                        }

                        if (isFalse(s)) {
                            return 0.0F;
                        }

                        try {
                            return Float.parseFloat(s);
                        } catch (NumberFormatException numberformatexception) {
                            LOGGER.warn("Invalid floating point value for option {} = {}", p_168464_, s, numberformatexception);
                        }
                    }

                    return p_168465_;
                }

                public <T> T process(String p_168470_, T p_168471_, Function<String, T> p_168472_, Function<T, String> p_168473_) {
                    String s = this.getValueOrNull(p_168470_);
                    return (T)(s == null ? p_168471_ : p_168472_.apply(s));
                }
            });

            KeyMapping.resetMapping();
        } catch (Exception exception) {
            LOGGER.error("Failed to load %s mod options".formatted(BetterSound.MODID), exception);
        }

    }

    public void save() {
        try(final PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8))) {
            printwriter.println("PianoKeyBinds:");
            this.processOptions(new Options.FieldAccess() {
                public void writePrefix(String string) {
                    printwriter.print(string);
                    printwriter.print(':');
                }

                @Override
                public <T> void process(String pName, OptionInstance<T> pValue) {
                    DataResult<JsonElement> dataresult = pValue.codec().encodeStart(JsonOps.INSTANCE, pValue.get());
                    dataresult.error().ifPresent((p_232133_) -> {
                        LOGGER.error("{} {}: {}", "Error saving %s mod option".formatted(BetterSound.MODID), pValue, p_232133_);
                    });
                    dataresult.result().ifPresent((p_232140_) -> {
                        this.writePrefix(pName);
                        printwriter.println(GSON.toJson(p_232140_));
                    });
                }

                @Override
                public int process(String pName, int pValue) {
                    this.writePrefix(pName);
                    printwriter.println(pValue);
                    return pValue;
                }

                @Override
                public boolean process(String pName, boolean pValue) {
                    this.writePrefix(pName);
                    printwriter.println(pValue);
                    return pValue;
                }

                @Override
                public String process(String pName, String pValue) {
                    this.writePrefix(pName);
                    printwriter.println(pValue);
                    return pValue;
                }

                @Override
                public float process(String pName, float pValue) {
                    this.writePrefix(pName);
                    printwriter.println(pValue);
                    return pValue;
                }

                @Override
                public <T> T process(String pName, T pValue, Function<String, T> pStringValuefier, Function<T, String> pValueStringifier) {
                    this.writePrefix(pName);
                    printwriter.println(pValueStringifier.apply(pValue));
                    return pValue;
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to save %s mod options".formatted(BetterSound.MODID), e);
        }
    }

    private void processOptions(Options.FieldAccess accessor) {
        this.processKeyMapping(accessor);
    }

    private void processKeyMapping(Options.FieldAccess accessor) {
        this.keys.forEach((keyMappingLazy, tone) -> {
            String s = keyMappingLazy.get().saveString() + (keyMappingLazy.get().getKeyModifier() != net.minecraftforge.client.settings.KeyModifier.NONE ? ":" + keyMappingLazy.get().getKeyModifier() : "");
            String s1 = accessor.process("key_" + keyMappingLazy.get().getName() + "." + tone, s);
            if (!s.equals(s1)) {
                if (s1.indexOf(':') != -1) {
                    String[] pts = s1.split(":");
                    keyMappingLazy.get().setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.valueFromString(pts[1]), InputConstants.getKey(pts[0].substring(0, 32)));
                } else
                    keyMappingLazy.get().setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.NONE, InputConstants.getKey(s1.substring(0, 32)));
            }
        });
        String s = keyPianoSustainPedal.get().saveString() + (keyPianoSustainPedal.get().getKeyModifier() != net.minecraftforge.client.settings.KeyModifier.NONE ? ":" + keyPianoSustainPedal.get().getKeyModifier() : "");
        String s1 = accessor.process("key_" + keyPianoSustainPedal.get().getName(), s);
        if (!s.equals(s1)) {
            if (s1.indexOf(':') != -1) {
                String[] pts = s1.split(":");
                keyPianoSustainPedal.get().setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.valueFromString(pts[1]), InputConstants.getKey(pts[0]));
            } else
                keyPianoSustainPedal.get().setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.NONE, InputConstants.getKey(s1));
        }
    }

    public void setKey(KeyMapping pKeyBinding, InputConstants.Key pInput) {
        pKeyBinding.setKey(pInput);
        this.save();
    }

    static boolean isTrue(String value) {
        return "true".equals(value);
    }

    static boolean isFalse(String pValue) {
        return "false".equals(pValue);
    }

    static {
        genPath = new File(Minecraft.getInstance().gameDirectory, "./config/bettersound");
        genPath.mkdirs();

        BLACK_KEYS = new Integer[] {
                GLFW.GLFW_KEY_2,
                GLFW.GLFW_KEY_3,
                GLFW.GLFW_KEY_5,
                GLFW.GLFW_KEY_6,
                GLFW.GLFW_KEY_7,
                GLFW.GLFW_KEY_9,
                GLFW.GLFW_KEY_0,
                GLFW.GLFW_KEY_EQUAL,
                GLFW.GLFW_KEY_BACKSPACE,

                GLFW.GLFW_KEY_A,
                GLFW.GLFW_KEY_D,
                GLFW.GLFW_KEY_F,
                GLFW.GLFW_KEY_H,
                GLFW.GLFW_KEY_J,
                GLFW.GLFW_KEY_K,
                GLFW.GLFW_KEY_SEMICOLON,
                GLFW.GLFW_KEY_APOSTROPHE,
        };
        WHITE_KEYS = new Integer[] {
                GLFW.GLFW_KEY_Q,
                GLFW.GLFW_KEY_W,
                GLFW.GLFW_KEY_E,
                GLFW.GLFW_KEY_R,
                GLFW.GLFW_KEY_T,
                GLFW.GLFW_KEY_Y,
                GLFW.GLFW_KEY_U,
                GLFW.GLFW_KEY_I,
                GLFW.GLFW_KEY_O,
                GLFW.GLFW_KEY_P,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                GLFW.GLFW_KEY_BACKSLASH,

                GLFW.GLFW_KEY_Z,
                GLFW.GLFW_KEY_X,
                GLFW.GLFW_KEY_C,
                GLFW.GLFW_KEY_V,
                GLFW.GLFW_KEY_B,
                GLFW.GLFW_KEY_N,
                GLFW.GLFW_KEY_M,
                GLFW.GLFW_KEY_COMMA,
                GLFW.GLFW_KEY_PERIOD,
                GLFW.GLFW_KEY_SLASH,
        };

        instance = new ModOptions();
    }
}
