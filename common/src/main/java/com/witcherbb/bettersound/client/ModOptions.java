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
import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.client.gui.PianoUtil;
import com.witcherbb.bettersound.common.platform.Platform;
import net.minecraft.client.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModOptions {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Gson GSON = new Gson();
    public static final File genPath;
    private static final ModOptions instance;
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    private final Fonts fonts = new Fonts();
    private final File optionsFile;

    private static final Integer[] BLACK_KEYS;
    private static final Integer[] WHITE_KEYS;

    private final Map<Supplier<KeyMapping>, Integer> pianokeys = new LinkedHashMap<>();
    private final Supplier<KeyMapping> keyPianoSustainPedal = () ->
            Platform.hooks().createGuiKeyMapping(
                    "key.bettersound.piano_pedal",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_SPACE,
                    "key.categories.bettersound.keyboard"
            );

    public final List<Supplier<KeyMapping>> keymappings = new ArrayList<>();

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
        for (int i = 0; i < 27; i++) {
            pianokeys.put(() ->
                    Platform.hooks().createGuiKeyMapping(
                            "key.bettersound.keyboard.key",
                            InputConstants.Type.KEYSYM,
                            GLFW.GLFW_KEY_UNKNOWN,
                            "key.categories.bettersound.keyboard"
                    ), i);
        }
        for (int i = 27; i < 66; i++) {
            try {
                int key = PianoUtil.isBlackey(i) ? BLACK_KEYS[blackCount++] : WHITE_KEYS[whiteCount++];
                pianokeys.put(() ->
                        Platform.hooks().createGuiKeyMapping(
                                "key.bettersound.keyboard.key",
                                InputConstants.Type.KEYSYM,
                                key,
                                "key.categories.bettersound.keyboard"
                        ), i);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        for (int i = 66; i < 88; i++) {
            pianokeys.put(() ->
                    Platform.hooks().createGuiKeyMapping(
                            "key.bettersound.keyboard.key",
                            InputConstants.Type.KEYSYM,
                            GLFW.GLFW_KEY_UNKNOWN,
                            "key.categories.bettersound.keyboard"
                    ), i);
        }
        keymappings.addAll(pianokeys.keySet());
        keymappings.add(keyPianoSustainPedal);
    }

    public static ModOptions getOptions() {
        return instance;
    }

    public void load() {
        try {
            FileToIdConverter fonts = FileToIdConverter.json("font");
            for (ResourceLocation font : fonts.listMatchingResources(Minecraft.getInstance().getResourceManager()).keySet()) {
                String s = font.getPath().split("/")[1];
                if (!s.endsWith(".json")) continue;
                String fontname = s.split(".json")[0];
                if (font.getNamespace().equals("minecraft")) this.fonts.putVanillaFont(fontname);
                else this.fonts.putModFont(font.getNamespace(), fontname);
            }

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

            processKeyMapping((name, defaultValue) -> compoundtag.contains(name) ? compoundtag.getString(name) : defaultValue);

            KeyMapping.resetMapping();
        } catch (Exception exception) {
            LOGGER.error("Failed to load %s mod options".formatted(Constants.MOD_ID), exception);
        }

    }

    public void save() {
        try(final PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8))) {
            printwriter.println("PianoKeyBinds:");
            writeKeyMappings((name, value) -> {
                printwriter.print(name);
                printwriter.print(':');
                printwriter.println(value);
            });
        } catch (Exception e) {
            LOGGER.error("Failed to save %s mod options".formatted(Constants.MOD_ID), e);
        }
    }

    /**
     * 读写 options 文件里那一行字符串的入口。
     *
     * <p>原来用的是 Forge 的 {@code Options.FieldAccess}——那个类型在 vanilla 里不是 public
     * （Forge 打了补丁），common 侧编译不过，所以换成自己的小接口。
     */
    @FunctionalInterface
    private interface OptionAccess {
        String get(String name, String defaultValue);
    }

    private void processKeyMapping(OptionAccess accessor) {
        this.pianokeys.forEach((keyMappingLazy, tone) -> {
            KeyMapping mapping = keyMappingLazy.get();
            String s = Platform.hooks().serializeKeyMapping(mapping);
            String s1 = accessor.get("key_" + mapping.getName() + "." + tone, s);
            if (!s.equals(s1)) {
                Platform.hooks().applySerializedKeyMapping(mapping, s1);
            }
        });
        KeyMapping pedal = keyPianoSustainPedal.get();
        String s = Platform.hooks().serializeKeyMapping(pedal);
        String s1 = accessor.get("key_" + pedal.getName(), s);
        if (!s.equals(s1)) {
            Platform.hooks().applySerializedKeyMapping(pedal, s1);
        }
    }

    /** 与 {@link #processKeyMapping} 对称的写出逻辑。 */
    private void writeKeyMappings(java.util.function.BiConsumer<String, String> writer) {
        this.pianokeys.forEach((keyMappingLazy, tone) -> {
            KeyMapping mapping = keyMappingLazy.get();
            writer.accept("key_" + mapping.getName() + "." + tone, Platform.hooks().serializeKeyMapping(mapping));
        });
        KeyMapping pedal = keyPianoSustainPedal.get();
        writer.accept("key_" + pedal.getName(), Platform.hooks().serializeKeyMapping(pedal));
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

    public Map<Supplier<KeyMapping>, Integer> getPianokeys() {
        return pianokeys;
    }

    public Supplier<KeyMapping> getKeyPianoSustainPedal() {
        return keyPianoSustainPedal;
    }

    public ResourceLocation getFont(String name) {
        String[] strings = name.split(":");
        if (strings.length == 1) return fonts.getVanillaFont(name);
        else if (strings.length == 2) {
            if (strings[0].equals("minecraft")) return fonts.getVanillaFont(strings[1]);
            else return fonts.getModFont(strings[0], strings[1]);
        }
        return null;
    }

    public ResourceLocation getVanillaFont(String name) {
        return fonts.getVanillaFont(name);
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

    static class Fonts {
        private final Map<String, ResourceLocation> modFonts = new HashMap<>();
        private final Map<String, ResourceLocation> vanillaFonts = new HashMap<>();

        void putVanillaFont(String fontName) {
            vanillaFonts.put(fontName, new ResourceLocation(fontName));
        }

        void putModFont(String modid, String fontName) {
            modFonts.put(modid + ":" + fontName, new ResourceLocation(Constants.MOD_ID, fontName));
        }

        void put(ResourceLocation location) {
            modFonts.put(location.getPath(), location);
        }

        ResourceLocation getModFont(String modid, String fontname) {
            return modFonts.get(modid + ":" + fontname);
        }

        ResourceLocation getVanillaFont(String name) {
            return modFonts.get(name);
        }
    }
}
