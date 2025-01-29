package ru.hogeltbellai.CloudixNetwork.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.impl.CPlayerManager;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;
import ru.hogeltbellai.Core.packet.PacketMutePlayer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "mute", permission = "network.mute", playerTabComplete = {0})
public class MuteCommand extends BaseCommand {

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Используйте - /" + label + " [игрок] [время] [причина]"));
            return true;
        }

        String target = args[0];
        if (target.length() > 20) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Некорректный ник"));
            return true;
        }

        long duration = CPlayerManager.parseDuration(args[1]);
        if (duration <= 0) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Некорректное время"));
            return true;
        }

        String reason = (args.length > 2) ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Не указана";

        Player targetPlayer = Bukkit.getPlayer(target);
        if (CPlayerManager.isMuted(target)) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX", "Игрок уже замучен"));
            return true;
        }

        if (CPlayerManager.mutePlayer(target, duration, TimeUnit.MILLISECONDS, reason, sender.getName())) {
            U.msg(sender, T.success("&#25B5FA&lCLOUDIX", "Вы успешно замутили " + target));
            if (targetPlayer != null) {
                U.bcast("");
                U.bcast("&#AE1313&lНАКАЗАНИЕ&#AE1313:");
                U.bcast("&#FAEDCA" + sender.getName() + " замутил игрока " + target + " на " + CPlayerManager.getRemainingMuteTime(target) + " по причине: &c" + CPlayerManager.getMuteReason(target));
                U.bcast("");
            } else {
                CNPluginSpigot.core().getCoreConnector().sendPacket(new PacketMutePlayer(target, sender.getName(), reason));
            }
        }
        return false;
    }
}