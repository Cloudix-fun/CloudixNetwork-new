package ru.hogeltbellai.CloudixNetwork.core;

import com.bivashy.auth.api.AuthPlugin;
import com.bivashy.auth.api.account.Account;
import com.bivashy.auth.api.database.AccountDatabase;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.CloudixNetwork.CNPluginBungee;
import ru.hogeltbellai.Core.PacketHandler;
import ru.hogeltbellai.Core.connector.CoreNetwork;
import ru.hogeltbellai.Core.packet.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @Override
    public void handlePacketAccountList(IoSession session, PacketAccountList packet) {
        String senderName = packet.sender;

        AuthPlugin authPlugin = AuthPlugin.instance();
        AccountDatabase accountDatabase = authPlugin.getAccountDatabase();

        CompletableFuture<Account> accountFuture = accountDatabase.getAccountFromName(senderName);

        accountFuture.thenAccept(account -> {
            if (account == null || account.getLastIpAddress() == null) {
                CoreNetwork.sendPacketResponse(session, packet, new PacketAccountList(senderName, null));
                return;
            }

            String playerIp = account.getLastIpAddress();

            CompletableFuture<Collection<Account>> allAccountsFuture = accountDatabase.getAllAccounts();

            allAccountsFuture.thenAccept(accounts -> {
                List<String> accountsList = accounts.stream()
                        .filter(acc -> playerIp.equals(acc.getLastIpAddress()))
                        .map(Account::getName)
                        .collect(Collectors.toList());

                if (accountsList.isEmpty()) {
                    CoreNetwork.sendPacketResponse(session, packet, new PacketAccountList(senderName, List.of(senderName)));
                } else {
                    CoreNetwork.sendPacketResponse(session, packet, new PacketAccountList(senderName, accountsList));
                }
            }).exceptionally(ex -> {
                CoreNetwork.sendPacketResponse(session, packet, new PacketAccountList(senderName, null));
                ex.printStackTrace();
                return null;
            });
        }).exceptionally(ex -> {
            CoreNetwork.sendPacketResponse(session, packet, new PacketAccountList(senderName, null));
            ex.printStackTrace();
            return null;
        });
    }
}