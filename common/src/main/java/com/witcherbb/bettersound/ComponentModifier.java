package com.witcherbb.bettersound;

import com.witcherbb.bettersound.client.ModOptions;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;

public class ComponentModifier {
    final static ModOptions modOptions = ModOptions.getOptions();
    private final MutableComponent component;

    private ComponentModifier(Component component) {
        this.component = component.copy();
    }

    public static ComponentModifier start(Component component) {
        return new ComponentModifier(component);
    }

    public ComponentModifier withColor(int color) {
        this.component.withStyle(Style.EMPTY.withColor(color));
        return this;
    }

    public ComponentModifier withBold(boolean bold) {
        this.component.withStyle(Style.EMPTY.withBold(bold));
        return this;
    }

    public ComponentModifier withItalic(boolean italic) {
        this.component.withStyle(Style.EMPTY.withItalic(italic));
        return this;
    }

    public ComponentModifier withUnderlined(boolean underlined) {
        this.component.withStyle(Style.EMPTY.withUnderlined(underlined));
        return this;
    }

    public ComponentModifier withStrikethrough(boolean strikethrough) {
        this.component.withStyle(Style.EMPTY.withStrikethrough(strikethrough));
        return this;
    }

    public ComponentModifier withObfuscated(boolean obfuscated) {
        this.component.withStyle(Style.EMPTY.withObfuscated(obfuscated));
        return this;
    }

    public ComponentModifier withClickEvent(ClickEvent clickEvent) {
        this.component.withStyle(Style.EMPTY.withClickEvent(clickEvent));
        return this;
    }

    public ComponentModifier withHoverEvent(HoverEvent hoverEvent) {
        this.component.withStyle(Style.EMPTY.withHoverEvent(hoverEvent));
        return this;
    }

    public ComponentModifier withInsertion(String insertion) {
        this.component.withStyle(Style.EMPTY.withInsertion(insertion));
        return this;
    }

    public ComponentModifier withFont(String fontName) {
        this.component.withStyle(Style.EMPTY.withFont(modOptions.getFont(fontName)));
        return this;
    }

    public ComponentModifier withFont(ResourceLocation font) {
        this.component.withStyle(Style.EMPTY.withFont(font));
        return this;
    }

    public MutableComponent end() {
        return this.component;
    }
}
