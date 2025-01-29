package ru.hogeltbellai.CloudixNetwork.api.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;

public class ChatAPI {

    public void globalMessage(String formattedMessage) {
        Bukkit.broadcastMessage(formattedMessage);
    }

    public void localMessage(Player sender, String formattedMessage, int chatRadius) {
        sender.getWorld().getPlayers().stream()
                .filter(recipient -> recipient != sender && recipient.getLocation().distance(sender.getLocation()) <= chatRadius)
                .forEach(recipient -> recipient.sendMessage(formattedMessage));
    }

    public String getPrefix(Player sender) {
        return CNPluginSpigot.core().getLuckPermsAPI().getGroupManager()
                .getGroup(CNPluginSpigot.core().getLuckPermsAPI().getUserManager().getUser(sender.getUniqueId()).getPrimaryGroup())
                .getCachedData().getMetaData().getPrefix();
    }
}
