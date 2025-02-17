package ru.hogeltbellai.Core;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.packet.*;

public class PacketHandler {
    public void handle(IoSession session, Packet packet) {}
    public void handleAnswer(IoSession session, PacketAnswer packet) {}
    public void handlePacketMessage(IoSession session, PacketMessage packet) {}
    public void handlePacketPrivateMessage(IoSession session, PacketPrivateMessage packet) {}
    public void handlePacketKickPlayer(IoSession session, PacketKickPlayer packet) {}
    public void handlePacketMutePlayer(IoSession session, PacketMutePlayer packet) {}
    public void handlePacketUpdater(IoSession session, PacketUpdater packet) throws Exception {}
    public void handlePacketGetPlayerInfo(IoSession session, PacketGetPlayerInfo packet) throws Exception {}
    public void handlePacketPlayerInfo(IoSession session, PacketPlayerInfo packet) {}
}
