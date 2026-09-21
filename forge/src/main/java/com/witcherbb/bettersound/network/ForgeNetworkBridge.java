package com.witcherbb.bettersound.network;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.witcherbb.bettersound.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Forge 侧网络实现：{@code SimpleChannel}。
 *
 * <p>common 的 {@link ModNetwork} 只负责描述包与发送语义，这里负责真正的通道、注册与分发。
 */
public final class ForgeNetworkBridge implements NetworkBridge {

    private final SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Constants.MOD_ID, "messages"))
            .networkProtocolVersion(() -> Constants.VERSION)
            .clientAcceptedVersions(Constants.VERSION::equals)
            .serverAcceptedVersions(Constants.VERSION::equals)
            .simpleChannel();

    private int nextPacketId;

    @Override
    public <T> void register(Class<T> type, PacketDirection direction,
                             BiConsumer<T, FriendlyByteBuf> encoder,
                             Function<FriendlyByteBuf, T> decoder,
                             PacketHandler<T> handler) {
        NetworkDirection forgeDirection = direction == PacketDirection.TO_SERVER
                ? NetworkDirection.PLAY_TO_SERVER
                : NetworkDirection.PLAY_TO_CLIENT;

        this.channel.messageBuilder(type, this.nextPacketId++, forgeDirection)
                .encoder((message, buf) -> encoder.accept(message, buf))
                .decoder(buf -> decoder.apply(buf))
                .consumerMainThread((message, context) -> {
                    handler.handle(message, new ForgePacketContext(context.get()));
                    context.get().setPacketHandled(true);
                })
                .add();
    }

    @Override
    public void sendToServer(Object packet) {
        this.channel.sendToServer(packet);
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        this.channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @Override
    public void broadcast(Object packet) {
        this.channel.send(PacketDistributor.ALL.noArg(), packet);
    }

    @Override
    public void broadcastBut(Object packet, ServerPlayer except) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player != except) {
                this.channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    /** 把 Forge 的 {@link NetworkEvent.Context} 适配成 common 的 {@link PacketContext}。 */
    private record ForgePacketContext(NetworkEvent.Context context) implements PacketContext {

        @Override
        public ServerPlayer sender() {
            return this.context.getSender();
        }

        @Override
        public void enqueueWork(Runnable task) {
            this.context.enqueueWork(task);
        }
    }
}
