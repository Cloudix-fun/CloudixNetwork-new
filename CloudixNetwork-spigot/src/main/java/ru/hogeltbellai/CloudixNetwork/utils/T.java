package ru.hogeltbellai.CloudixNetwork.utils;

import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.impl.CPlayerManager;

import java.util.List;

public class T {

    public static String system(String title, String text) {
        return "" + title + "&#B5B5B5 ➡ &f" + text;
    }

    public static String error(String title, String text) {
        return system(title, "&c" + text);
    }

    public static String success(String title, String text) {
        return system(title, "&f" + text);
    }

    public static String bantitle(String target) {
        List<String> banMessage = CNPluginSpigot.core().getConfig().getStringList("messages.ban_title");

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

    public static String mutetitle(String target) {
        String muteMessage = CNPluginSpigot.core().getConfig().getString("messages.mute_message");

        String sender = CPlayerManager.getMutedBy(target);
        String reason = CPlayerManager.getMuteReason(target);
        String time = CPlayerManager.getRemainingMuteTime(target);

        muteMessage = muteMessage.replace("%sender%", sender).replace("%reason%", reason).replace("%time%", time);

        return muteMessage;
    }
}
