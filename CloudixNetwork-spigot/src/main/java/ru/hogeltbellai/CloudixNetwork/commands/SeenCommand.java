package ru.hogeltbellai.CloudixNetwork.commands;

import org.bukkit.command.CommandSender;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;
import ru.hogeltbellai.Core.packet.PacketAccountList;
import ru.hogeltbellai.Core.packet.PacketAnswer;

import java.util.stream.Collectors;

@CommandInfo(
        name = "seen",
        permission = "network.seen",
        forAll = true,
        playerTabComplete = {0}
)
public class SeenCommand extends BaseCommand {

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Используйте - /" + label + " [игрок]"));
            return true;
        }

        String target = args[0];
        if (target.length() > 20) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Некорректный ник"));
            return true;
        }

        PacketAccountList request = new PacketAccountList(target, null);

        CNPluginSpigot.core().getCoreConnector().sendRequest(request, response -> {
            if (response instanceof PacketAccountList) {
                PacketAccountList accountListResponse = (PacketAccountList) response;

                if (accountListResponse.accountNames == null || accountListResponse.accountNames.isEmpty()) {
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Информация о аккаунтах не найдена."));
                } else {
                    String formattedAccounts = accountListResponse.accountNames.stream()
                            .map(account -> isBanned(account) ? "§c" + account : "§7" + account)
                            .collect(Collectors.joining(", "));

                    U.msg(sender, "&#25B5FAИнформация &fо " + target + ":");
                    U.msg(sender, "");
                    U.msg(sender, "&#25B5FA - &fАккаунты&8: " + formattedAccounts);
                }
            }
        }, 500L);
        return true;
    }

    public static boolean isBanned(String playerName) {
        String sql = "SELECT COUNT(*) FROM bans WHERE username = ? AND status = 1 AND end_time > ?";
        long currentTimeMillis = System.currentTimeMillis();
        Integer count = CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }, playerName, currentTimeMillis);
        return count != null && count > 0;
    }
}
