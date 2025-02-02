package ru.hogeltbellai.CloudixNetwork.impl.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.impl.CPlayerManager;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;

public class CPlayerChat {

    public static void globalMessage(Player player, String formattedMessage) {
        if(CPlayerManager.isMuted(player.getName())) {
            U.msg(player, T.error("&#AE1313&lНАКАЗАНИЕ", T.mutetitle(player.getName())));
            return;
        }
        Bukkit.broadcastMessage(formattedMessage);
    }

    public static void localMessage(Player player, String formattedMessage, int chatRadius) {
        if(CPlayerManager.isMuted(player.getName())) {
            U.msg(player, T.error("&#AE1313&lНАКАЗАНИЕ", T.mutetitle(player.getName())));
            return;
        }
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.getWorld().equals(player.getWorld())) {
                if (recipient.getLocation().distance(player.getLocation()) <= chatRadius) {
                    recipient.sendMessage(formattedMessage);
                }
            }
        }
    }

    public static String getPrefix(Player player) {
        return CNPluginSpigot.core().getLuckPermsAPI().getGroupManager()
                .getGroup(CNPluginSpigot.core().getLuckPermsAPI().getUserManager().getUser(player.getUniqueId()).getPrimaryGroup())
                .getCachedData().getMetaData().getPrefix();
    }
}
