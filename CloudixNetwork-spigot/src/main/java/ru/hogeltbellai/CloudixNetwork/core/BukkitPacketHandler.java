package ru.hogeltbellai.CloudixNetwork.core;

import org.apache.mina.core.session.IoSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.impl.CPlayerManager;
import ru.hogeltbellai.CloudixNetwork.utils.S;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;
import ru.hogeltbellai.Core.PacketHandler;
import ru.hogeltbellai.Core.packet.PacketKickPlayer;
import ru.hogeltbellai.Core.packet.PacketMessage;
import ru.hogeltbellai.Core.packet.PacketMutePlayer;
import ru.hogeltbellai.Core.packet.PacketPrivateMessage;

public class BukkitPacketHandler extends PacketHandler {

    private final CNPluginSpigot plugin;

    public BukkitPacketHandler(CNPluginSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handlePacketMessage(IoSession session, PacketMessage packet) {
        S.runSync(() -> {

        }, plugin);
    }

    @Override
    public void handlePacketKickPlayer(IoSession session, PacketKickPlayer packet) {
        S.runSync(() -> {
            Player player = Bukkit.getPlayer(packet.target);
            if(player != null) {
                if (packet.kickType == PacketKickPlayer.KickType.BAN) {
                    U.bcast("&e" + packet.sender + "&c забанил игрока &e" + packet.target + "&c на " + CPlayerManager.getRemainingBanTime(packet.target) + " по причине: &f" + CPlayerManager.getBanReason(packet.target));
                    player.kickPlayer(U.colored(T.bantitle(packet.target)));
                }
            }
        }, plugin);
    }

    @Override
    public void handlePacketMutePlayer(IoSession session, PacketMutePlayer packet) {
        S.runSync(() -> {
            Player player = Bukkit.getPlayer(packet.target);
            if(player != null) {
                U.bcast("&e" + packet.sender + "&c замутил игрока &e" + packet.target + "&c на " + CPlayerManager.getRemainingMuteTime(packet.target) + " по причине: &f" + CPlayerManager.getMuteReason(packet.target));
            }
        }, plugin);
    }

    @Override
    public void handlePacketPrivateMessage(IoSession session, PacketPrivateMessage packet) {
        Player player = Bukkit.getPlayerExact(packet.target);
        if(player != null) {
            U.msg(player, "&e[" + packet.sender + " < - &fВы&e] &f" + packet.message);
        }
    }
}
