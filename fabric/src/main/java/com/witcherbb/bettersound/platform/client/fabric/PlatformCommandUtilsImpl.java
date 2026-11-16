package com.witcherbb.bettersound.platform.client.fabric;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class PlatformCommandUtilsImpl {
    private static final Supplier<CommandSourceStack> CLIENT_SOURCE = Suppliers.memoize(() -> {
        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 pos = player != null ? player.position() : Vec3.ZERO;
        Vec2 rot = player != null ? new Vec2(player.getXRot(), player.getYRot()) : Vec2.ZERO;
        return new CommandSourceStack(CommandSource.NULL,
                pos, rot, null, 2, "", Component.empty(), null, player);
    });

    public static BlockPos getBlockPos(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> commandContext, String name) {
        return commandContext.getArgument(name, Coordinates.class).getBlockPos(CLIENT_SOURCE.get());
    }
}
