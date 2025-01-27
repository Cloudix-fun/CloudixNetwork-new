package ru.hogeltbellai.CloudixNetwork.utils;

import ru.hogeltbellai.cloudixnetwork.CNPlugin;
import ru.hogeltbellai.cloudixnetwork.impl.CPlayerManager;

import java.util.List;

public class T {

    public static String system(String title, String text) {
        return "&7[" + title + "&7] &f" + text;
    }

    public static String warning(String title, String text) {
        return system(title, "&6" + text);
    }

    public static String error(String title, String text) {
        return system(title, "&c" + text);
    }

    public static String success(String title, String text) {
        return system(title, "&#29F52F" + text);
    }

    public static String bantitle(String target) {
        List<String> banMessage = CNPlugin.core().getConfig().getStringList("messages.ban_title");

        return banMessage.stream()
                .map(message -> message
                        .replace("%sender%", CPlayerManager.getBannedBy(target))
                        .replace("%reason%", CPlayerManager.getBanReason(target))
                        .replace("%time%", CPlayerManager.getRemainingBanTime(target))
                )
                .reduce((line1, line2) -> line1 + "\n" + line2)
                .map(result -> result.length() > 256 ? result.substring(0, 256) : result)
                .orElse("");
    }
}
