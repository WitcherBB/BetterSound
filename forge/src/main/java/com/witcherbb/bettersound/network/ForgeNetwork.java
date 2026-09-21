package com.witcherbb.bettersound.network;

import com.witcherbb.bettersound.network.protocol.server.SJukeboxNamePacket;
import com.witcherbb.bettersound.network.protocol.server.SExampleNameChangedPacket;
import com.witcherbb.bettersound.network.protocol.server.SJukeboxControllerNamePacket;
import com.witcherbb.bettersound.network.protocol.server.SNoteBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyPressedPacket;
import com.witcherbb.bettersound.network.protocol.server.piano.SPianoKeyReleasedPacket;
import com.witcherbb.bettersound.network.protocol.server.SBlockEntityDataChangePacket;
import com.witcherbb.bettersound.network.protocol.server.nbs.SNBSPlayPacket;
import com.witcherbb.bettersound.network.protocol.server.nbs.SAutoPlayerActionPacket;
import com.witcherbb.bettersound.network.protocol.server.midi.SMidiPlayPacket;
import com.witcherbb.bettersound.network.protocol.client.CJukeboxNameConfirmPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockPlayNotePacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockStopPacket;
import com.witcherbb.bettersound.network.protocol.client.piano.CPianoBlockPlayMultipleNotesPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CCommandPlayNBSPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSStopPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPausePacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSPlayOnPacket;
import com.witcherbb.bettersound.network.protocol.client.nbs.CNBSReloadPacket;

/**
 * Forge 侧的数据包清单 + 桥实例。
 *
 * <p>包暂时留在 forge（它们还依赖同样留在 forge 的方块/实体/菜单/音效类），
 * 等内容类都具备搬进 common 的条件后，这份清单会跟着包一起挪到 common 的 {@link ModNetwork}。
 */
public final class ForgeNetwork {

    public static final ForgeNetworkBridge BRIDGE = new ForgeNetworkBridge();

    /** 把所有数据包登记到 {@link #BRIDGE} 上（在 mod 初始化阶段调用一次）。 */
    public static void register() {
        BRIDGE.register(SJukeboxNamePacket.class, PacketDirection.TO_SERVER,
                SJukeboxNamePacket::encode, SJukeboxNamePacket::decode, SJukeboxNamePacket::handle);
        BRIDGE.register(SExampleNameChangedPacket.class, PacketDirection.TO_SERVER,
                SExampleNameChangedPacket::encode, SExampleNameChangedPacket::decode, SExampleNameChangedPacket::handle);
        BRIDGE.register(SJukeboxControllerNamePacket.class, PacketDirection.TO_SERVER,
                SJukeboxControllerNamePacket::encode, SJukeboxControllerNamePacket::decode, SJukeboxControllerNamePacket::handle);
        BRIDGE.register(SNoteBlockPlayNotePacket.class, PacketDirection.TO_SERVER,
                SNoteBlockPlayNotePacket::encode, SNoteBlockPlayNotePacket::decode, SNoteBlockPlayNotePacket::handle);
        BRIDGE.register(SPianoKeyPressedPacket.class, PacketDirection.TO_SERVER,
                SPianoKeyPressedPacket::encode, SPianoKeyPressedPacket::decode, SPianoKeyPressedPacket::handle);
        BRIDGE.register(SPianoKeyReleasedPacket.class, PacketDirection.TO_SERVER,
                SPianoKeyReleasedPacket::encode, SPianoKeyReleasedPacket::decode, SPianoKeyReleasedPacket::handle);
        BRIDGE.register(SBlockEntityDataChangePacket.class, PacketDirection.TO_SERVER,
                SBlockEntityDataChangePacket::encode, SBlockEntityDataChangePacket::decode, SBlockEntityDataChangePacket::handle);
        BRIDGE.register(SNBSPlayPacket.class, PacketDirection.TO_SERVER,
                SNBSPlayPacket::encode, SNBSPlayPacket::decode, SNBSPlayPacket::handle);
        BRIDGE.register(SAutoPlayerActionPacket.class, PacketDirection.TO_SERVER,
                SAutoPlayerActionPacket::encode, SAutoPlayerActionPacket::decode, SAutoPlayerActionPacket::handle);
        BRIDGE.register(SMidiPlayPacket.class, PacketDirection.TO_SERVER,
                SMidiPlayPacket::encode, SMidiPlayPacket::decode, SMidiPlayPacket::handle);
        BRIDGE.register(CJukeboxNameConfirmPacket.class, PacketDirection.TO_CLIENT,
                CJukeboxNameConfirmPacket::encode, CJukeboxNameConfirmPacket::decode, CJukeboxNameConfirmPacket::handle);
        BRIDGE.register(CPianoBlockPlayNotePacket.class, PacketDirection.TO_CLIENT,
                CPianoBlockPlayNotePacket::encode, CPianoBlockPlayNotePacket::decode, CPianoBlockPlayNotePacket::handle);
        BRIDGE.register(CPianoBlockStopPacket.class, PacketDirection.TO_CLIENT,
                CPianoBlockStopPacket::encode, CPianoBlockStopPacket::decode, CPianoBlockStopPacket::handle);
        BRIDGE.register(CPianoBlockPlayMultipleNotesPacket.class, PacketDirection.TO_CLIENT,
                CPianoBlockPlayMultipleNotesPacket::encode, CPianoBlockPlayMultipleNotesPacket::decode, CPianoBlockPlayMultipleNotesPacket::handle);
        BRIDGE.register(CCommandPlayNBSPacket.class, PacketDirection.TO_CLIENT,
                CCommandPlayNBSPacket::encode, CCommandPlayNBSPacket::decode, CCommandPlayNBSPacket::handle);
        BRIDGE.register(CNBSStopPacket.class, PacketDirection.TO_CLIENT,
                CNBSStopPacket::encode, CNBSStopPacket::decode, CNBSStopPacket::handle);
        BRIDGE.register(CNBSPausePacket.class, PacketDirection.TO_CLIENT,
                CNBSPausePacket::encode, CNBSPausePacket::decode, CNBSPausePacket::handle);
        BRIDGE.register(CNBSPlayOnPacket.class, PacketDirection.TO_CLIENT,
                CNBSPlayOnPacket::encode, CNBSPlayOnPacket::decode, CNBSPlayOnPacket::handle);
        BRIDGE.register(CNBSReloadPacket.class, PacketDirection.TO_CLIENT,
                CNBSReloadPacket::encode, CNBSReloadPacket::decode, CNBSReloadPacket::handle);
    }

    private ForgeNetwork() {
    }
}
