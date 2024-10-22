package com.witcherbb.bettersound.mixins.mixins;

import com.witcherbb.bettersound.mixins.extenders.AbstractWidgetExtender;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements AbstractWidgetExtender {
    @Shadow public abstract boolean isFocused();

    @Redirect(method = "updateTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;setTooltipForNextRenderPass(Lnet/minecraft/client/gui/components/Tooltip;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Z)V"))
    private void updateTooltip0(Screen instance, Tooltip pTooltip, ClientTooltipPositioner pPositioner, boolean pOverride) {
        instance.setTooltipForNextRenderPass(pTooltip, pPositioner, this.betterSound$ifOverridePreTip());
    }

    @Override
    public boolean betterSound$ifOverridePreTip() {
        return this.isFocused();
    }
}
