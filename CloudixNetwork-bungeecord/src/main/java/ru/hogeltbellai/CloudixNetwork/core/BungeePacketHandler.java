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
    public void handlePacketUpdater(IoSession session, PacketUpdater packet) {
        CoreNetwork.broadcast(packet);
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

        if (bungeePlayer == null) {
            CoreNetwork.sendPacketResponse(session, packet, new PacketAnswer("NotFound"));
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
            CoreNetwork.sendPacketResponse(session, packet, new PacketAnswer("YouIgnoreAll"));
            return;
        }

        if (isTargetIgnoringAll) {
            CoreNetwork.sendPacketResponse(session, packet, new PacketAnswer("RecIgnoreAll"));
            return;
        }

        if (isSenderIgnoringTarget) {
            CoreNetwork.sendPacketResponse(session, packet, new PacketAnswer("YouIgnorePlayer"));
            return;
        }

        if (isTargetIgnoringSender) {
            CoreNetwork.sendPacketResponse(session, packet, new PacketAnswer("RecIgnoreYou"));
            return;
        }

        CoreNetwork.sendPacketResponse(session, packet, new PacketAnswer("Found"));
        CoreNetwork.broadcast(privateMessage);
    }

    @Override
    public void handlePacketGetPlayerInfo(IoSession session, PacketGetPlayerInfo packet) throws Exception {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.playerName);
        if (player == null) {
            CoreNetwork.sendPacketResponse(session, packet, new PacketPlayerInfo(packet.playerName, null));
            return;
        }
        String serverName = player.getServer() != null ? player.getServer().getInfo().getName() : null;
        CoreNetwork.sendPacketResponse(session, packet, new PacketPlayerInfo(packet.playerName, serverName));
    }
}