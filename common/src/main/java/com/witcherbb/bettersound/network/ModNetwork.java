package com.witcherbb.bettersound.network;

import net.minecraft.server.level.ServerPlayer;

/**
 * 与 loader 无关的网络门面：调用点（方块、菜单、屏幕、播放器）只认这里的静态方法，
 * 具体通道由 loader 通过 {@link #init(NetworkBridge)} 注入。
 *
 * <p>数据包清单目前放在各 loader 模块（forge 侧是 {@code ForgeNetwork}），
 * 等包本身也具备搬进 common 的条件后，这份清单会一起挪到这里。
 */
public final class ModNetwork {

    private static NetworkBridge bridge;

    /** loader 在 mod 初始化时注入自己的实现。 */
    public static void init(NetworkBridge networkBridge) {
        bridge = networkBridge;
    }

    private static NetworkBridge bridge() {
        if (bridge == null) {
            throw new IllegalStateException("ModNetwork 未初始化：loader 侧要先调用 ModNetwork.init(...)");
        }
        return bridge;
    }

    public static void sendToServer(Object packet) {
        bridge().sendToServer(packet);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        bridge().sendToPlayer(packet, player);
    }

    public static void broadcast(Object packet) {
        bridge().broadcast(packet);
    }

    /** 广播给除 {@code except} 以外的所有人。 */
    public static void broadcastBut(Object packet, ServerPlayer except) {
        bridge().broadcastBut(packet, except);
    }

    private ModNetwork() {
    }
}
