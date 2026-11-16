package com.witcherbb.bettersound.platform.client.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.witcherbb.bettersound.fabric.mixins.mixins.KeyMappingAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public final class ClientHooksImpl {
    public static KeyMapping createGuiKeyMapping(String name, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, type, keyCode, category);
    }

    public static String serializeKeyMapping(KeyMapping mapping) {
        return mapping.saveString();
    }

    public static void applySerializedKeyMapping(KeyMapping mapping, String serialized) {
        mapping.setKey(InputConstants.getKey(serialized));
    }

    public static boolean isActiveAndMatchesKey(KeyMapping mapping, InputConstants.Key key) {
        return key != InputConstants.UNKNOWN && key.equals(((KeyMappingAccessor) mapping).key());
    }

    public static boolean hasKeyModifierConflict(KeyMapping mapping1, KeyMapping mapping2) {
        return false;
    }
}
