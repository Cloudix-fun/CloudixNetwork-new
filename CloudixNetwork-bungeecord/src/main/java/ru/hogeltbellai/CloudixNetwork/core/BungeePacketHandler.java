package ru.hogeltbellai.CloudixNetwork.core;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.CloudixNetwork.CNPluginBungee;
import ru.hogeltbellai.Core.PacketHandler;
import ru.hogeltbellai.Core.connector.CoreNetwork;
import ru.hogeltbellai.Core.packet.*;

public class BungeePacketHandler extends PacketHandler {
    private final CNPluginBungee plugin;

    public BungeePacketHandler(CNPluginBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handlePacketMessage(IoSession session, PacketMessage packet) {
        CoreNetwork.broadcast(packet);
    }

    @Override
    public void handlePacketKickPlayer(IoSession session, PacketKickPlayer packet) {
        CoreNetwork.broadcast(packet);
    }

    @Override
    public void handlePacketMutePlayer(IoSession session, PacketMutePlayer packet) {
        CoreNetwork.broadcast(packet);
    }

    @Override
    public void handlePacketPrivateMessage(IoSession session, PacketPrivateMessage packet) {
        PacketPrivateMessage privateMessage = packet;

        String senderName = privateMessage.sender;
        String targetName = privateMessage.target;

        ProxiedPlayer bungeePlayer = ProxyServer.getInstance().getPlayer(targetName);
        PacketAnswer packetAnswer;

        if (bungeePlayer == null) {
            packetAnswer = new PacketAnswer("NotFound");
            packetAnswer.pResponseId = packet.pResponseId;
            privateMessage.pResponseId = packet.pResponseId;
            session.write(packetAnswer);
            return;
        }

        int senderUserId = CNPluginBungee.core().getMysqlPlayer().getUserId(senderName);
        int targetUserId = CNPluginBungee.core().getMysqlPlayer().getUserId(targetName);

        boolean isSenderIgnoringAll = Boolean.parseBoolean(CNPluginBungee.core().getMysqlPlayer().getMeta(senderUserId, "ignore_all"));
        boolean isTargetIgnoringAll = Boolean.parseBoolean(CNPluginBungee.core().getMysqlPlayer().getMeta(targetUserId, "ignore_all"));

        String senderIgnoreKey = "ignore_" + targetUserId;
        boolean isSenderIgnoringTarget = CNPluginBungee.core().getMysqlPlayer().hasMeta(senderUserId, senderIgnoreKey);

        String targetIgnoreKey = "ignore_" + senderUserId;
        boolean isTargetIgnoringSender = CNPluginBungee.core().getMysqlPlayer().hasMeta(targetUserId, targetIgnoreKey);

        if (isSenderIgnoringAll) {
            packetAnswer = new PacketAnswer("YouIgnoreAll");
            packetAnswer.pResponseId = packet.pResponseId;
            privateMessage.pResponseId = packet.pResponseId;
            session.write(packetAnswer);
            return;
        }

        if (isTargetIgnoringAll) {
            packetAnswer = new PacketAnswer("RecIgnoreAll");
            packetAnswer.pResponseId = packet.pResponseId;
            privateMessage.pResponseId = packet.pResponseId;
            session.write(packetAnswer);
            return;
        }

        if (isSenderIgnoringTarget) {
            packetAnswer = new PacketAnswer("YouIgnorePlayer");
            packetAnswer.pResponseId = packet.pResponseId;
            privateMessage.pResponseId = packet.pResponseId;
            session.write(packetAnswer);
            return;
        }

        if (isTargetIgnoringSender) {
            packetAnswer = new PacketAnswer("RecIgnoreYou");
            packetAnswer.pResponseId = packet.pResponseId;
            privateMessage.pResponseId = packet.pResponseId;
            session.write(packetAnswer);
            return;
        }

        packetAnswer = new PacketAnswer("Found");
        packetAnswer.pResponseId = packet.pResponseId;
        privateMessage.pResponseId = packet.pResponseId;
        session.write(packetAnswer);
        CoreNetwork.broadcast(privateMessage);
    }
}