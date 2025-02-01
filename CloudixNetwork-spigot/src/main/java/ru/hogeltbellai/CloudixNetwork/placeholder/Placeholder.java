package ru.hogeltbellai.CloudixNetwork.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;

public class Placeholder extends PlaceholderExpansion {

    @Override
    public @NotNull
    String getIdentifier() {
        return "network";
    }

    @Override
    public @NotNull
    String getAuthor() {
        return "HogeltBellai";
    }

    @Override
    public @NotNull
    String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("pix")) {
            int pix = CNPluginSpigot.core().getDatabase().executeQuery("SELECT pix FROM users WHERE username = ?", resultSet -> resultSet.next() ? resultSet.getInt("pix") : 0, player.getName());
            return String.valueOf(pix);
        }

        return null;
    }
}