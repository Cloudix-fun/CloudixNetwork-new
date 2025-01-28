package ru.hogeltbellai.CloudixNetwork.commands;

import org.bukkit.command.CommandSender;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.api.commands.SubCommandInfo;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;
import ru.hogeltbellai.Core.packet.PacketMessage;
import ru.hogeltbellai.Core.packet.PacketUpdater;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "cloudix", permission = "network.admin")
public class CloudixCommand extends BaseCommand {

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            U.msg(sender, T.system("&#08FB36&lCLOUDIX", "Важная админская команда >:3"));
            return false;
        }
        return executeSubCommand(sender, label, args);
    }

    @SubCommandInfo(name = "stats", permission = "network.stats")
    public boolean statsSubCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Runtime runtime = Runtime.getRuntime();
            List<String> lines = new ArrayList<>();
            lines.add("&7------------ &aСтатистика &7------------");
            lines.add("&fRAM память: &f" + ((runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L) + " MB / " + (runtime.totalMemory() / 1024L / 1024L) + " MB up to " + (runtime.maxMemory() / 1024L / 1024L) + " MB");
            lines.add("&fСоединение с БД: &f" + (CNPluginSpigot.core().getDatabase().isConnected() ? "&aактивно" : "&cразорвано"));
            lines.add("&fЗапросов к БД: &f" + (CNPluginSpigot.core().getDatabase().getQueryCount()));
            lines.add("&fСоединение с Mina: &f" + (CNPluginSpigot.core().getCoreConnector().isConnected() ? "&aактивно" : "&cразорвано"));
            U.msg(sender, lines);
            return true;
        }
        return false;
    }

    @SubCommandInfo(name = "updater", permission = "network.updater")
    public boolean updateSubCommand(CommandSender sender, String label, String[] args) throws Exception {
        if (args.length == 0) {
            U.msg(sender, T.success("&#08FB36&lCLOUDIX", "Установка обновления прошла успешно, рестарт через 10 сек."));
            CNPluginSpigot.core().getCoreConnector().sendPacket(new PacketMessage(U.colored(T.success("&#08FB36&lCLOUDIX", "Обновление! Рестарт через 10 сек."))));

            CNPluginSpigot.core().getCoreConnector().sendPacket(new PacketUpdater());
            return true;
        }
        return false;
    }
}