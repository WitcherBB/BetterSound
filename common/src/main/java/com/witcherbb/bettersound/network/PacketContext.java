package com.witcherbb.bettersound.network;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * 收到数据包时的上下文，由各 loader 的网络实现适配（Forge 是 {@code NetworkEvent.Context}，
 * Fabric 是 {@code ServerPlayNetworking} 回调参数）。
 */
public interface PacketContext {

    /** 服务端收到客户端包时的发送者；客户端侧或拿不到时为 {@code null}。 */
    @Nullable
    ServerPlayer sender();

    /** 把逻辑丢到主线程执行（Forge 必须这么做；Fabric 本来就回调在主线程，可直接运行）。 */
    void enqueueWork(Runnable task);
}
