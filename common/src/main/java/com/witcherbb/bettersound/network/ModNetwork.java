package com.witcherbb.bettersound.network;

import com.witcherbb.bettersound.Constants;
import com.witcherbb.bettersound.network.protocol.client.CBetterJukeboxNameConfirmPacket;
import com.witcherbb.bettersound.network.protocol.client.COpenPianoScreenPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CCommandPlayNBSPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSReloadPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSStopPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoPlayMultipleNotesPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoPlaySingleNotePacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoStopNotesExceptPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoStopSingleNotePacket;
import com.witcherbb.bettersound.network.protocol.server.SBetterJukeboxNamePacket;
import com.witcherbb.bettersound.network.protocol.server.SBlockEntityDataChangePacket;
import com.witcherbb.bettersound.network.protocol.server.SExampleNameChangedPacket;
import com.witcherbb.bettersound.network.protocol.server.SJukeboxControllerNamePacket;
import com.witcherbb.bettersound.network.protocol.server.SNoteBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.server.midi.SMidiPlayPacket;
import com.witcherbb.bettersound.network.protocol.server.nbs.SAutoPlayerActionPacket;
import com.witcherbb.bettersound.network.protocol.server.nbs.SNBSPlayPacket;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyPressedPacket;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyReleasedPacket;
import dev.architectury.networking.NetworkChannel;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.GameInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 全 mod 唯一的网络门面：通道、数据包登记、发送都走 Architectury API，
 * common / forge / fabric 三个模块共用这一份实现，loader 侧不再需要任何网络代码。
 *
 * <p>各 loader 的入口只需在 mod 初始化时调用一次 {@link #register()}：
 * forge 在 {@code FMLCommonSetupEvent} 里，fabric 在 {@code onInitialize()} 里。
 *
 * <p>包名前缀即方向约定：{@code S*} = Server-bound（客户端 → 服务端，由客户端 {@code sendToServer} 发出），
 * {@code C*} = Client-bound（服务端 → 客户端，由服务端 {@code sendToPlayer}/{@code broadcast} 发出）。
 *
 * <p>注意：{@link NetworkChannel#register} 会同时登记 C2S 与 S2C 两个接收器
 * （服务端只登记 C2S），包 id 由类名派生的 UUID 稳定生成，所以两侧调用顺序无关；
 * 也正因为方向不在 API 层强制，{@code handle} 里必须自己保证只在正确的侧执行
 * （例如 S2C 包的处理逻辑会在客户端跑，不要在里面做服务端才有的操作）。
 */
public final class ModNetwork {

    /** 通道 id：{@code bettersound:main}。 */
    public static final ResourceLocation CHANNEL_ID = new ResourceLocation(Constants.MOD_ID, "main");

    /** Architectury 的通道封装：行为上类似 Forge 的 {@code SimpleChannel}，但跨平台。 */
    public static final NetworkChannel CHANNEL = NetworkChannel.create(CHANNEL_ID);

    private static boolean registered;

    /**
     * 登记所有数据包。两侧（客户端 / 服务端）各调用一次；重复调用是安全的空操作。
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        // ===== 客户端 -> 服务端（C2S）：S 前缀 = Server-bound =====
        CHANNEL.register(SExampleNameChangedPacket.class,
                SExampleNameChangedPacket::encode, SExampleNameChangedPacket::decode, SExampleNameChangedPacket::handle);
        CHANNEL.register(SJukeboxControllerNamePacket.class,
                SJukeboxControllerNamePacket::encode, SJukeboxControllerNamePacket::decode, SJukeboxControllerNamePacket::handle);
        CHANNEL.register(SNoteBlockPlayNotePacket.class,
                SNoteBlockPlayNotePacket::encode, SNoteBlockPlayNotePacket::decode, SNoteBlockPlayNotePacket::handle);
        CHANNEL.register(SPianoKeyPressedPacket.class,
                SPianoKeyPressedPacket::encode, SPianoKeyPressedPacket::decode, SPianoKeyPressedPacket::handle);
        CHANNEL.register(SPianoKeyReleasedPacket.class,
                SPianoKeyReleasedPacket::encode, SPianoKeyReleasedPacket::decode, SPianoKeyReleasedPacket::handle);
        CHANNEL.register(SBlockEntityDataChangePacket.class,
                SBlockEntityDataChangePacket::encode, SBlockEntityDataChangePacket::decode, SBlockEntityDataChangePacket::handle);
        CHANNEL.register(SNBSPlayPacket.class,
                SNBSPlayPacket::encode, SNBSPlayPacket::decode, SNBSPlayPacket::handle);
        CHANNEL.register(SAutoPlayerActionPacket.class,
                SAutoPlayerActionPacket::encode, SAutoPlayerActionPacket::decode, SAutoPlayerActionPacket::handle);
        CHANNEL.register(SMidiPlayPacket.class,
                SMidiPlayPacket::encode, SMidiPlayPacket::decode, SMidiPlayPacket::handle);
        CHANNEL.register(SBetterJukeboxNamePacket.class,
                SBetterJukeboxNamePacket::encode, SBetterJukeboxNamePacket::decode, SBetterJukeboxNamePacket::handle);

        // ===== 服务端 -> 客户端（S2C）：C 前缀 = Client-bound =====
        // 这些包的 handle 只对客户端有意义（内部会碰 Minecraft / ClientLevel 等 client-only 类）。
        // 专用服务端上给 handle 建方法句柄会立刻“链接-校验”该方法，从而去加载 ClientLevel，
        // 被 Forge 的 RuntimeDistCleaner 拦下（invalid dist DEDICATED_SERVER）。
        // 服务端永远收不到 S2C 包，但需要 encode 才能发出去，所以走 registerS2C：
        // 客户端登记真 handler，服务端登记空 handler，且 handler 用 Supplier 延迟求值，绝不提前建句柄。
        registerS2C(CPianoPlaySingleNotePacket.class,
                CPianoPlaySingleNotePacket::encode, CPianoPlaySingleNotePacket::decode,
                () -> CPianoPlaySingleNotePacket::handle);
        registerS2C(CPianoStopSingleNotePacket.class,
                CPianoStopSingleNotePacket::encode, CPianoStopSingleNotePacket::decode,
                () -> CPianoStopSingleNotePacket::handle);
        registerS2C(CPianoStopNotesExceptPacket.class,
                CPianoStopNotesExceptPacket::encode, CPianoStopNotesExceptPacket::decode,
                () -> CPianoStopNotesExceptPacket::handle);
        registerS2C(CPianoPlayMultipleNotesPacket.class,
                CPianoPlayMultipleNotesPacket::encode, CPianoPlayMultipleNotesPacket::decode,
                () -> CPianoPlayMultipleNotesPacket::handle);
        registerS2C(CCommandPlayNBSPacket.class,
                CCommandPlayNBSPacket::encode, CCommandPlayNBSPacket::decode,
                () -> CCommandPlayNBSPacket::handle);
        registerS2C(CNBSStopPacket.class,
                CNBSStopPacket::encode, CNBSStopPacket::decode,
                () -> CNBSStopPacket::handle);
        registerS2C(CNBSPausePacket.class,
                CNBSPausePacket::encode, CNBSPausePacket::decode,
                () -> CNBSPausePacket::handle);
        registerS2C(CNBSPlayOnPacket.class,
                CNBSPlayOnPacket::encode, CNBSPlayOnPacket::decode,
                () -> CNBSPlayOnPacket::handle);
        registerS2C(CNBSReloadPacket.class,
                CNBSReloadPacket::encode, CNBSReloadPacket::decode,
                () -> CNBSReloadPacket::handle);
        registerS2C(COpenPianoScreenPacket.class,
                COpenPianoScreenPacket::encode, COpenPianoScreenPacket::decode,
                () -> COpenPianoScreenPacket::handle);
        registerS2C(CBetterJukeboxNameConfirmPacket.class,
                CBetterJukeboxNameConfirmPacket::encode, CBetterJukeboxNameConfirmPacket::decode,
                () -> CBetterJukeboxNameConfirmPacket::handle);
    }

    /**
     * 登记一个 S2C 包。
     *
     * <p>专用服务端不需要（也收不到）S2C 包，但发送时要用 {@code encoder}，所以两侧都要登记；
     * 区别只在 handler：服务端不能链接客户端 handler，否则会因加载 client-only 类被 DistCleaner 拦下。
     * 因此 handler 用 {@link Supplier} 传入——服务端那个分支根本不会去求值它。
     */
    private static <T> void registerS2C(Class<T> type,
                                        BiConsumer<T, FriendlyByteBuf> encoder,
                                        Function<FriendlyByteBuf, T> decoder,
                                        Supplier<BiConsumer<T, Supplier<NetworkManager.PacketContext>>> handler) {
        CHANNEL.register(type, encoder, decoder,
                Platform.getEnvironment() == Env.CLIENT ? handler.get() : (message, context) -> {
                });
    }

    /** 客户端 -> 服务端。只在客户端调用（Architectury 的该方法带 {@code @Environment(CLIENT)}）。 */
    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    /** 服务端 -> 指定玩家。 */
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.sendToPlayer(player, packet);
    }

    /** 服务端 -> 所有玩家。 */
    public static void broadcast(Object packet) {
        List<ServerPlayer> players = players();
        if (!players.isEmpty()) {
            CHANNEL.sendToPlayers(players, packet);
        }
    }

    /** 服务端 -> 除 {@code except} 以外的所有玩家。 */
    public static void broadcastBut(Object packet, ServerPlayer except) {
        List<ServerPlayer> targets = players().stream().filter(player -> player != except).toList();
        if (!targets.isEmpty()) {
            CHANNEL.sendToPlayers(targets, packet);
        }
    }

    /** 当前服务端的在线玩家；不在服务端环境（或服务端未就绪）时返回空表。 */
    private static List<ServerPlayer> players() {
        MinecraftServer server = GameInstance.getServer();
        return server == null ? List.of() : server.getPlayerList().getPlayers();
    }

    private ModNetwork() {
    }
}
