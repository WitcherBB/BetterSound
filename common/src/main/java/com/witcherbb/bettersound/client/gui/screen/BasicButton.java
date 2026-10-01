package com.witcherbb.bettersound.client.gui.screen;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BasicButton extends Button {
    protected BasicButton(int x, int y, int width, int height, Component message, OnPress onPress,
            CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    protected BasicButton(BasicButton.Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, builder.createNarration);
    }

    public static BasicButton.Builder basicBuilder(Component message, OnPress onPress) {
        return new BasicButton.Builder(message, onPress);
    }

    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private CreateNarration createNarration;

        public static Builder create(Component message, Button.OnPress onPress) {
            return new Builder(message, onPress);
        }

        public Builder(Component message, OnPress onPress) {
            this.createNarration = Supplier<MutableComponent>::get;
            this.message = message;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public BasicButton build(Function<BasicButton.Builder, BasicButton> getter) {
            return getter.apply(this);
        }
    }
}
