package com.witcherbb.bettersound.network;

/** 数据包方向：由 loader 侧的网络实现翻译成自己的方向常量。 */
public enum PacketDirection {
    /** 客户端 → 服务端 */
    TO_SERVER,
    /** 服务端 → 客户端 */
    TO_CLIENT
}
