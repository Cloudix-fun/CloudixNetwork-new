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
import ru.hogeltbellai.Core.packet.PacketKickPlayer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "ban", permission = "network.ban", playerTabComplete = {0})
public class BanCommand extends BaseCommand {

    public BanCommand() {
        super();
    }

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            U.msg(sender, T.error("&#2FFD45Cloudix", "Используйте &7- &e/" + label + " [игрок] [время] [причина]"));
            return true;
        }

        String target = args[0];
        if (target.length() > 20) {
            U.msg(sender, T.error("&#2FFD45Cloudix", "Ник игрока не может быть больше 20 символов"));
            return true;
        }

        long duration = CPlayerManager.parseDuration(args[1]);
        if (duration <= 0) {
            U.msg(sender, T.error("&#2FFD45Cloudix", "Некорректное число"));
            return true;
        }

        String reason = (args.length > 2) ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Не указана";

        Player targetPlayer = Bukkit.getPlayer(target);
        if (!CPlayerManager.isBanned(target)) {
            if (CPlayerManager.banPlayer(target, duration, TimeUnit.MILLISECONDS, reason, sender.getName())) {
                if (targetPlayer != null) {
                    U.bcast("&e" + sender.getName() + "&c забанил игрока &e" + targetPlayer.getName() + "&c на " + CPlayerManager.getRemainingBanTime(targetPlayer.getName()) + " по причине: &f" + CPlayerManager.getBanReason(targetPlayer.getName()));
                    targetPlayer.kickPlayer(U.colored(T.bantitle(target)));
                } else {
                    CNPluginSpigot.core().getCoreConnector().sendPacket(new PacketKickPlayer(PacketKickPlayer.KickType.BAN, target, sender.getName(), reason));
                }
                U.msg(sender, T.success("&#2FFD45Cloudix", "Вы успешно забанили &f" + target));
            }
        }
        return false;
    }
}