package ru.hogeltbellai.CloudixNetwork.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class U {

    public static void msg(Player player, String... msg) {
        player.sendMessage(colored(msg));
    }

    public static void msg(Player player, List<String> msg) {
        for (String str : msg)
            player.sendMessage(colored(str));
    }

    public static void msg(CommandSender sender, String... msg) {
        sender.sendMessage(colored(msg));
    }

    public static void msg(CommandSender sender, List<String> msg) {
        for (String str : msg)
            sender.sendMessage(colored(str));
    }

    public static void bcast(String msg) {
        Bukkit.broadcastMessage(colored(msg));
    }

    public static void title(Player player, String header, String footer, int in, int stay, int out) {
        player.sendTitle(colored(header), colored(footer), in, stay, out);
    }

    public static String colored(String str) {
        if (str == null)
            return null;
        return translate(str.replace("&", "§"));
    }

    public static String[] colored(String... lines) {
        if (lines == null)
            return null;
        for (int i = 0; i < lines.length; i++)
            lines[i] = colored(lines[i]);
        return lines;
    }

    public static List<String> colored(List<String> lines) {
        ListIterator<String> it = lines.listIterator();
        while (it.hasNext())
            it.set(colored(it.next()));
        return lines;
    }

    private static String translate(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
            matcher = pattern.matcher(message);
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
    }
}
