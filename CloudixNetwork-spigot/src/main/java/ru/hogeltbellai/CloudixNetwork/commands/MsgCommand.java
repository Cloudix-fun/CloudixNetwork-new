package ru.hogeltbellai.CloudixNetwork.commands;

import org.bukkit.command.CommandSender;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.Core.packet.PacketPrivateMessage;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;
import ru.hogeltbellai.Core.packet.PacketAnswer;

import java.util.Arrays;

@CommandInfo(name = "msg", forPlayer = true, playerTabComplete = {0})
public class MsgCommand extends BaseCommand {

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX","Используйте - /" + label + " [игрок] [сообщение]"));
            return true;
        }

        String target = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if(target.equalsIgnoreCase(sender.getName())) {
            U.msg(sender, "&7Одиночество...");
            return true;
        }

        trySendPrivateMessage(sender, target, message);
        return false;
    }

    private void trySendPrivateMessage(CommandSender sender, String target, String message) {
        PacketPrivateMessage request = new PacketPrivateMessage(sender.getName(), target, message);
        CNPluginSpigot.core().getCoreConnector().sendRequest(request, response -> {
            PacketAnswer answer = (PacketAnswer) response;
            switch (answer.status) {
                case "NotFound":
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Игрок " + target + " не в сети!"));
                    return;
                case "YouIgnoreAll":
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Вы игнорируете все сообщения"));
                    return;
                case "YouIgnorePlayer":
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Вы игнорируете игрока " + target));
                    return;
                case "RecIgnoreAll":
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Игрок " + target + " игнорирует все сообщения"));
                    return;
                case "RecIgnoreYou":
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Игрок " + target + " игнорирует вас"));
                    return;
                case "Found":
                    U.msg(sender, "&f&lЛС &fВы&#B5B5B5 ➡ " + target + ": &f" + message);
                    return;
                default:
                    U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "&cПроизошла ошибка, не удалось отправить сообщение"));
                    return;
            }
        }, 200L);
    }
}
