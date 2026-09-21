package com.witcherbb.bettersound.network;

import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * 网络桥：common 只描述「有哪些包、怎么编解码、怎么处理、发给谁」，
 * 真正的通道、注册与发送由各 loader 实现。
 *
 * <ul>
 *   <li>Forge：{@code SimpleChannel}（见 forge 模块的 {@code ForgeNetworkBridge}）</li>
 *   <li>Fabric：{@code ServerPlayNetworking} / {@code ClientPlayNetworking}</li>
 * </ul>
 */
public interface NetworkBridge {

    /**
     * 登记一个数据包。
     *
     * @param type      包类型（loader 侧通常需要它来分配 id / 做分发）
     * @param direction 传输方向
     * @param encoder   编码器（约等于 {@code packet::encode}）
     * @param decoder   解码器（约等于 {@code Packet::decode}）
     * @param handler   处理器（约等于 {@code Packet::handle}）
     */
    <T> void register(Class<T> type, PacketDirection direction,
                      BiConsumer<T, FriendlyByteBuf> encoder,
                      Function<FriendlyByteBuf, T> decoder,
                      PacketHandler<T> handler);

    void sendToServer(Object packet);

    void sendToPlayer(Object packet, ServerPlayer player);

    void broadcast(Object packet);

    /** 广播给除某个玩家以外的所有人。 */
    void broadcastBut(Object packet, ServerPlayer except);
}
