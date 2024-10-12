package com.witcherbb.bettersound.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ModKeyMapping {
    public static final Lazy<KeyMapping> KEY_PIANO_PEDAL = Lazy.of(() ->
            new KeyMapping(
                    "key.bettersound.piano_pedal",
                    KeyConflictContext.GUI,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_SPACE,
                    "key.categories.bettersound.keyboard"
            ));

    public static final Lazy<KeyMapping> FIRST = Lazy.of(() ->
            new KeyMapping(
                    "key.bettersound.keyboard.1",
                    KeyConflictContext.GUI,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_A,
                    "key.categories.bettersound.keyboard"
            ));
}
