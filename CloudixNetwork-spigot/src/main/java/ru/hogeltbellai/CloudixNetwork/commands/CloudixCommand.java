package ru.hogeltbellai.CloudixNetwork.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.api.commands.SubCommandInfo;
import ru.hogeltbellai.CloudixNetwork.updater.Updater;
import ru.hogeltbellai.CloudixNetwork.utils.S;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "cloudix", permission = "network.admin")
public class CloudixCommand extends BaseCommand {

    public CloudixCommand() {
        super();
    }

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player && !sender.hasPermission("network.admin")) {
                U.msg(sender, T.warning("&#2FFD45Cloudix", "Куда ты лезешь, оно тебя сожрёт!"));
                return true;
            }

            U.msg(sender, T.system("&#2FFD45Cloudix", "Важная админская команда"));
            return false;
        }
        return executeSubCommand(sender, label, args);
    }

    @SubCommandInfo(name = "stats", permission = "network.stats")
    public boolean statsSubCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Runtime runtime = Runtime.getRuntime();
            List<String> lines = new ArrayList<>();
            lines.add("&e------------ &fСтатистика &e------------");
            lines.add("&eRAM память: &f" + ((runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L) + " MB / " + (runtime.totalMemory() / 1024L / 1024L) + " MB up to " + (runtime.maxMemory() / 1024L / 1024L) + " MB");
            lines.add("&eСоединение с БД: &f" + (CNPluginSpigot.core().getDatabase().isConnected() ? "&aактивно" : "&cразорвано"));
            lines.add("&eЗапросов к БД: &f" + (CNPluginSpigot.core().getDatabase().getQueryCount()));
            lines.add("&eСоединение с Mina: &f" + (CNPluginSpigot.core().getCoreConnector().isConnected() ? "&aактивно" : "&cразорвано"));
            U.msg(sender, lines);
            return true;
        }
        return false;
    }

    @SubCommandInfo(name = "updater", permission = "network.updater")
    public boolean updateSubCommand(CommandSender sender, String label, String[] args) throws Exception {
        if (args.length == 0) {
            Updater.performUpdate("CloudixNetwork-2.2.jar");
            U.msg(sender, T.success("&#2FFD45Cloudix", "Установка обновления прошла успешно, рестарт через 5 сек."));

            S.delay(5, CNPluginSpigot.core(), () -> {
                Bukkit.getServer().spigot().restart();
            });
            return true;
        }
        return false;
    }
}