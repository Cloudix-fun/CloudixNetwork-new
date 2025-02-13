package ru.hogeltbellai.CloudixNetwork.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;

@CommandInfo(
        name = "ignore",
        forPlayer = true,
        playerTabComplete = {0}
)
public class IgnoreCommand extends BaseCommand {
    private static final String IGNORE_ALL_KEY = "ignore_all";

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        int userId = CNPluginSpigot.core().getMysqlPlayer().getUserId(player.getName());

        if (args.length == 0) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX","Используйте - /" + label + " [игрок]"));
            return true;
        }

        String target = args[0];

        if(target.equalsIgnoreCase(sender.getName())) {
            U.msg(player, T.error("&#25B5FA&lCLOUDIX", "Нельзя игнорировать себя-же"));
            return true;
        }

        if ("@all".equalsIgnoreCase(target)) {
            handleIgnoreAll(player, userId);
        } else {
            handleIgnorePlayer(player, userId, target);
        }

        return true;
    }

    private void handleIgnoreAll(Player player, int userId) {
        boolean isIgnoringAll = Boolean.parseBoolean(CNPluginSpigot.core().getMysqlPlayer().getMeta(userId, IGNORE_ALL_KEY));
        if (isIgnoringAll) {
            CNPluginSpigot.core().getMysqlPlayer().setMeta(userId, IGNORE_ALL_KEY, "false");
            U.msg(player, T.success("&#25B5FA&lCLOUDIX", "Вы включили личные сообщения"));
        } else {
            CNPluginSpigot.core().getMysqlPlayer().setMeta(userId, IGNORE_ALL_KEY, "true");
            U.msg(player, T.success("&#25B5FA&lCLOUDIX", "Вы выключили личные сообщения"));
        }
    }

    private void handleIgnorePlayer(Player player, int userId, String target) {
        if(CNPluginSpigot.core().getMysqlPlayer().getUserId(target) == null) {
            U.msg(player, T.error("&#25B5FA&lCLOUDIX", "Игрок " + target + " не найден в базе данных"));
            return;
        }

        int targetId = CNPluginSpigot.core().getMysqlPlayer().getUserId(target);
        String key = "ignore_" + targetId;
        boolean isIgnoring = CNPluginSpigot.core().getMysqlPlayer().hasMeta(userId, key);

        if (isIgnoring) {
            CNPluginSpigot.core().getMysqlPlayer().deleteMeta(userId, key);
            U.msg(player, T.success("&#25B5FA&lCLOUDIX", "Игрок " + target + " больше не игнорируется"));
        } else {
            CNPluginSpigot.core().getMysqlPlayer().setMeta(userId, key, "true");
            U.msg(player, T.success("&#25B5FA&lCLOUDIX", "Игрок " + target + " теперь игнорируется"));
        }
    }
}
