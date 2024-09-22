package com.witcherbb.bettersound.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModPacketDistributor {
    public static final PacketDistributor<ServerPlayer> ALL_EXCEPT = new PacketDistributor<>(ModPacketDistributor::playerListExcepted, NetworkDirection.PLAY_TO_CLIENT);

    private static Consumer<Packet<?>> playerListExcepted(
            PacketDistributor<ServerPlayer> distributor,
            Supplier<ServerPlayer> serverPlayerSupplier) {
        return packet -> {
            List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
            ServerPlayer exceptedPlayer = serverPlayerSupplier.get();
            players.forEach((player) -> {
                if (player != exceptedPlayer) {
                    player.connection.send(packet);
                }
            });
        };
    }
}
