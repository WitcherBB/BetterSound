package com.witcherbb.bettersound.platform.client.forge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

@OnlyIn(Dist.CLIENT)
public final class ClientHooksImpl {
    public static KeyMapping createGuiKeyMapping(String name, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, KeyConflictContext.GUI, type, keyCode, category);
    }

    public static String serializeKeyMapping(KeyMapping mapping) {
        return mapping.saveString() + (mapping.getKeyModifier() != KeyModifier.NONE ? ":" + mapping.getKeyModifier() : "");
    }

    public static void applySerializedKeyMapping(KeyMapping mapping, String serialized) {
        if (serialized.indexOf(':') != -1) {
            String[] parts = serialized.split(":");
            mapping.setKeyModifierAndCode(KeyModifier.valueFromString(parts[1]), InputConstants.getKey(parts[0]));
        } else {
            mapping.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.getKey(serialized));
        }
    }

    public static boolean isActiveAndMatchesKey(KeyMapping mapping, Key key) {
        return mapping.isActiveAndMatches(key);
    }

    public static boolean hasKeyModifierConflict(KeyMapping mapping1, KeyMapping mapping2) {
        return mapping1.hasKeyModifierConflict(mapping2);
    }
}
