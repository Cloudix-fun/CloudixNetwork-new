package ru.hogeltbellai.CloudixNetwork.api.commands;

import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;

import java.lang.reflect.Method;
import java.util.*;

public abstract class BaseCommand implements CommandExecutor, TabExecutor {
    private final String name;
    private final List<String> aliases;
    private final String permission;
    private final boolean forPlayer;
    private final boolean forConsole;
    private final boolean forAll;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public BaseCommand() {
        CommandInfo info = getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("Класс с аннотацией не найден: " + getClass().getName());
        }
        this.name = info.name();
        this.aliases = Arrays.asList(info.aliases());
        this.permission = info.permission();
        this.forPlayer = info.forPlayer();
        this.forConsole = info.forConsole();
        this.forAll = info.forAll();
        registerSubCommands();
    }

    public BaseCommand register(JavaPlugin plugin) {
        PluginCommand pluginCommand = plugin.getCommand(name);
        if (pluginCommand == null) {
            throw new IllegalStateException("Команда не найдена в plugin.yml: " + name);
        }

        pluginCommand.setExecutor(this);

        for (String alias : aliases) {
            PluginCommand aliasCommand = plugin.getCommand(alias);
            if (aliasCommand != null) {
                aliasCommand.setExecutor(this);
            }
        }

        return this;
    }

    private void registerSubCommands() {
        Method[] methods = getClass().getDeclaredMethods();
        for (Method method : methods) {
            SubCommandInfo subCommandInfo = method.getAnnotation(SubCommandInfo.class);
            if (subCommandInfo != null) {
                method.setAccessible(true);
                subCommands.put(subCommandInfo.name().toLowerCase(), new SubCommand(
                        subCommandInfo.permission(),
                        subCommandInfo.forPlayer(),
                        subCommandInfo.forConsole(),
                        subCommandInfo.forAll(),
                        subCommandInfo.playerTabComplete(),
                        (sender, label, args) -> {
                            try {
                                return (boolean) method.invoke(this, sender, label, args);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        }
                ));
            }
        }
    }

    protected abstract boolean executeCommand(CommandSender sender, String label, String[] args);

    protected boolean executeSubCommand(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                if (subCommand.logic == null) return false;

                if (subCommand.permission != null && !subCommand.permission.isEmpty() && sender instanceof org.bukkit.entity.Player &&
                        !sender.hasPermission(subCommand.permission)) {
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "У вас нет прав"));
                    return true;
                }

                if (subCommand.forPlayer && !(sender instanceof org.bukkit.entity.Player)) {
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Эта команда доступна только для игроков"));
                    return true;
                }

                if (subCommand.forConsole && !(sender instanceof ConsoleCommandSender)) {
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Эта команда доступна только для консоли"));
                    return true;
                }

                if (subCommand.forAll && !(sender instanceof org.bukkit.entity.Player || sender instanceof ConsoleCommandSender)) {
                    return true;
                }

                return subCommand.logic.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (permission != null && !permission.isEmpty() && sender instanceof org.bukkit.entity.Player && !sender.hasPermission(permission)) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "У вас нет прав"));
            return true;
        }

        if (forPlayer && !(sender instanceof org.bukkit.entity.Player)) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Эта команда доступна только для игроков"));
            return true;
        }
        if (forConsole && !(sender instanceof ConsoleCommandSender)) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Эта команда доступна только для консоли"));
            return true;
        }
        if (forAll && !(sender instanceof org.bukkit.entity.Player || sender instanceof ConsoleCommandSender)) {
            return true;
        }

        if (executeSubCommand(sender, label, args)) {
            return true;
        }

        return executeCommand(sender, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);
        if (commandInfo != null && args.length > 0) {
            int argIndex = args.length - 1;

            for (int playerTabIndex : commandInfo.playerTabComplete()) {
                if (argIndex == playerTabIndex) {
                    List<String> players = new ArrayList<>();
                    sender.getServer().getOnlinePlayers().forEach(player -> players.add(player.getName()));
                    return players;
                }
            }
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(subCommands.keySet());
            suggestions.removeIf(s -> subCommands.get(s).permission != null &&
                    sender instanceof org.bukkit.entity.Player &&
                    !sender.hasPermission(subCommands.get(s).permission));
            return suggestions;
        }

        if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                int argIndex = args.length - 2;
                for (int playerTabIndex : subCommand.getPlayerTabComplete()) {
                    if (argIndex == playerTabIndex) {
                        List<String> players = new ArrayList<>();
                        sender.getServer().getOnlinePlayers().forEach(player -> players.add(player.getName()));
                        return players;
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    @FunctionalInterface
    public interface SubCommandLogic {
        boolean execute(CommandSender sender, String label, String[] args);
    }

    public static class SubCommand {
        private final String permission;
        private final boolean forPlayer;
        private final boolean forConsole;
        private final boolean forAll;
        private final int[] playerTabComplete;
        private final SubCommandLogic logic;

        public SubCommand(String permission, boolean forPlayer, boolean forConsole, boolean forAll, int[] playerTabComplete, SubCommandLogic logic) {
            this.permission = permission;
            this.forPlayer = forPlayer;
            this.forConsole = forConsole;
            this.forAll = forAll;
            this.playerTabComplete = playerTabComplete;
            this.logic = logic;
        }

        public int[] getPlayerTabComplete() {
            return playerTabComplete;
        }
    }
}