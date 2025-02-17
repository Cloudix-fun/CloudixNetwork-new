package ru.hogeltbellai.CloudixNetwork.commands;

import com.bivashy.auth.api.AuthPlugin;
import com.bivashy.auth.api.account.Account;
import com.bivashy.auth.api.database.AccountDatabase;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SeenCommand extends Command {

    public SeenCommand() {
        super("seen", "auth.seen");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Использование: /seen <ник>");
            return;
        }

        String playerName = args[0];
        AuthPlugin authPlugin = AuthPlugin.instance();
        AccountDatabase accountDatabase = authPlugin.getAccountDatabase();
        CompletableFuture<Account> accountFuture = accountDatabase.getAccountFromName(playerName);

        accountFuture.thenAccept(account -> {
            if (account == null || account.getLastIpAddress() == null) {
                sender.sendMessage("§cАккаунт не найден или не зарегистрирован!");
                return;
            }

            String playerIp = account.getLastIpAddress();

            accountDatabase.getAllAccounts().thenAccept(accounts -> {
                List<String> accountsList = accounts.stream()
                        .filter(acc -> playerIp.equals(acc.getLastIpAddress()))
                        .map(acc -> acc.getName())
                        .collect(Collectors.toList());

                if (accountsList.isEmpty()) {
                    sender.sendMessage("§x§2§5§B§5§F§AИнформация §fо §7" + playerName);
                    sender.sendMessage("");
                    sender.sendMessage("IP: " + playerIp);
                    sender.sendMessage("Аккаунты: " + playerName);
                } else {
                    sender.sendMessage("§x§2§5§B§5§F§AИнформация §fо §7" + playerName);
                    sender.sendMessage("");
                    sender.sendMessage("IP: " + playerIp);
                    sender.sendMessage("Аккаунты: " + String.join(", ", accountsList));
                }
            }).exceptionally(ex -> {
                sender.sendMessage("§cПроизошла ошибка при получении информации о других аккаунтах.");
                return null;
            });
        }).exceptionally(ex -> {
            sender.sendMessage("§cПроизошла ошибка при получении информации о аккаунте.");
            ex.printStackTrace();
            return null;
        });
    }
}
