package com.witcherbb.bettersound.network;

import com.mojang.logging.LogUtils;
import com.witcherbb.bettersound.BetterSound;
import com.witcherbb.bettersound.network.protocol.*;
import com.witcherbb.bettersound.network.protocol.nbs.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ModNetwork {
    static final Logger LOGGER = LogUtils.getLogger();

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(BetterSound.MODID, "messages"))
                .networkProtocolVersion(() -> BetterSound.VERSION)
                .clientAcceptedVersions(clientVersion -> clientVersion.equals(BetterSound.VERSION))
                .serverAcceptedVersions(serverVersion -> serverVersion.equals(BetterSound.VERSION))
                .simpleChannel();

        INSTANCE = net;

        addServerPacket();
        addClientPacket();
    }

    private static void addServerPacket() {

        INSTANCE.messageBuilder(SJukeboxNamePacket.class, id())
                .encoder(SJukeboxNamePacket::encode)
                .decoder(SJukeboxNamePacket::decode)
                .consumerMainThread(SJukeboxNamePacket::handle)
                .add();

        INSTANCE.messageBuilder(SExampleNameChangedPacket.class, id())
                .encoder(SExampleNameChangedPacket::encode)
                .decoder(SExampleNameChangedPacket::new)
                .consumerMainThread(SExampleNameChangedPacket::handle)
                .add();

        INSTANCE.messageBuilder(SJukeboxControllerNamePacket.class, id())
                .encoder(SJukeboxControllerNamePacket::encode)
                .decoder(SJukeboxControllerNamePacket::decode)
                .consumerMainThread(SJukeboxControllerNamePacket::handle)
                .add();

        INSTANCE.messageBuilder(SNoteBlockPlayNotePacket.class, id())
                .encoder(SNoteBlockPlayNotePacket::encode)
                .decoder(SNoteBlockPlayNotePacket::new)
                .consumerMainThread(SNoteBlockPlayNotePacket::handle)
                .add();

        INSTANCE.messageBuilder(SPianoKeyPressedPacket.class, id())
                .encoder(SPianoKeyPressedPacket::encode)
                .decoder(SPianoKeyPressedPacket::decode)
                .consumerMainThread(SPianoKeyPressedPacket::handle)
                .add();

        INSTANCE.messageBuilder(SPianoKeyReleasedPacket.class, id())
                .encoder(SPianoKeyReleasedPacket::encode)
                .decoder(SPianoKeyReleasedPacket::decode)
                .consumerMainThread(SPianoKeyReleasedPacket::handle)
                .add();

        INSTANCE.messageBuilder(SToneKeyReleasedPacket.class, id())
                .encoder(SToneKeyReleasedPacket::encode)
                .decoder(SToneKeyReleasedPacket::decode)
                .consumerMainThread(SToneKeyReleasedPacket::handle)
                .add();

        INSTANCE.messageBuilder(SToneKeyPressedPacket.class, id())
                .encoder(SToneKeyPressedPacket::encode)
                .decoder(SToneKeyPressedPacket::decode)
                .consumerMainThread(SToneKeyPressedPacket::handle)
                .add();

        INSTANCE.messageBuilder(SBlockEntityDataChangePacket.class, id())
                .encoder(SBlockEntityDataChangePacket::encode)
                .decoder(SBlockEntityDataChangePacket::decode)
                .consumerMainThread(SBlockEntityDataChangePacket::handle)
                .add();

        INSTANCE.messageBuilder(SNBSPlayPacket.class, id())
                .encoder(SNBSPlayPacket::encode)
                .decoder(SNBSPlayPacket::decode)
                .consumerMainThread(SNBSPlayPacket::handle)
                .add();
    }

    private static void addClientPacket() {

        INSTANCE.messageBuilder(CJukeboxNameConfirmPacket.class, id())
                .encoder(CJukeboxNameConfirmPacket::encode)
                .decoder(CJukeboxNameConfirmPacket::decode)
                .consumerMainThread(CJukeboxNameConfirmPacket::handle)
                .add();

        INSTANCE.messageBuilder(CPianoBlockPlayNotePacket.class, id())
                .encoder(CPianoBlockPlayNotePacket::encode)
                .decoder(CPianoBlockPlayNotePacket::decode)
                .consumerMainThread(CPianoBlockPlayNotePacket::handle)
                .add();

        INSTANCE.messageBuilder(CPianoBlockStopPacket.class, id())
                .encoder(CPianoBlockStopPacket::encode)
                .decoder(CPianoBlockStopPacket::decode)
                .consumerMainThread(CPianoBlockStopPacket::handle)
                .add();

        INSTANCE.messageBuilder(CToneBlockPlayNotePacket.class, id())
                .encoder(CToneBlockPlayNotePacket::encode)
                .decoder(CToneBlockPlayNotePacket::decode)
                .consumerMainThread(CToneBlockPlayNotePacket::handle)
                .add();

        INSTANCE.messageBuilder(CToneBlockStopPacket.class, id())
                .encoder(CToneBlockStopPacket::encode)
                .decoder(CToneBlockStopPacket::decode)
                .consumerMainThread(CToneBlockStopPacket::handle)
                .add();

        INSTANCE.messageBuilder(CCommandPlayNBSPacket.class, id())
                .encoder(CCommandPlayNBSPacket::encode)
                .decoder(CCommandPlayNBSPacket::decode)
                .consumerMainThread(CCommandPlayNBSPacket::handle)
                .add();

        INSTANCE.messageBuilder(CPianoBlockPlayMultipleNotesPacket.class, id())
                .encoder(CPianoBlockPlayMultipleNotesPacket::encode)
                .decoder(CPianoBlockPlayMultipleNotesPacket::decode)
                .consumerMainThread(CPianoBlockPlayMultipleNotesPacket::handle)
                .add();

        INSTANCE.messageBuilder(CNBSStopPacket.class, id())
                .encoder(CNBSStopPacket::encode)
                .decoder(CNBSStopPacket::decode)
                .consumerMainThread(CNBSStopPacket::handle)
                .add();

        INSTANCE.messageBuilder(CNBSPausePacket.class, id())
                .encoder(CNBSPausePacket::encode)
                .decoder(CNBSPausePacket::decode)
                .consumerMainThread(CNBSPausePacket::handle)
                .add();

        INSTANCE.messageBuilder(CNBSPlayOnPacket.class, id())
                .encoder(CNBSPlayOnPacket::encode)
                .decoder(CNBSPlayOnPacket::decode)
                .consumerMainThread(CNBSPlayOnPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void broadcast(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void broadcastBut(MSG message, @NotNull ServerPlayer player) {
        INSTANCE.send(ModPacketDistributor.ALL_EXCEPT.with(() -> player), message);
    }
}
