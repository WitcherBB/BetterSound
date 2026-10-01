package com.witcherbb.bettersound.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.witcherbb.bettersound.common.platform.client.ClientHooks;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

@OnlyIn(Dist.CLIENT)
public final class ForgeClientHooks implements ClientHooks {
    @Override
    public KeyMapping createGuiKeyMapping(String name, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, KeyConflictContext.GUI, type, keyCode, category);
    }

    @Override
    public String serializeKeyMapping(KeyMapping mapping) {
        return mapping.saveString() + (mapping.getKeyModifier() != KeyModifier.NONE ? ":" + mapping.getKeyModifier() : "");
    }

    @Override
    public void applySerializedKeyMapping(KeyMapping mapping, String serialized) {
        if (serialized.indexOf(':') != -1) {
            String[] parts = serialized.split(":");
            mapping.setKeyModifierAndCode(KeyModifier.valueFromString(parts[1]), InputConstants.getKey(parts[0]));
        } else {
            mapping.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.getKey(serialized));
        }
    }

    @Override
    public boolean isActiveAndMatchesKey(KeyMapping mapping, Key key) {
        return mapping.isActiveAndMatches(key);
    }

    @Override
    public boolean hasKeyModifierConflict(KeyMapping mapping1, KeyMapping mapping2) {
        return mapping1.hasKeyModifierConflict(mapping2);
    }
}
