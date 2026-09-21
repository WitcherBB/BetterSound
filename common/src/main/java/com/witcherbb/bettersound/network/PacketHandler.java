package com.witcherbb.bettersound.network;

/** 数据包的处理逻辑：每个包的静态 {@code handle(T, PacketContext)} 都符合这个形状。 */
@FunctionalInterface
public interface PacketHandler<T> {
    void handle(T packet, PacketContext context);
}
